package com.welovecoding.nbeditorconfig.processor;

import static com.welovecoding.nbeditorconfig.config.Settings.ENCODING_SETTING;
import com.welovecoding.nbeditorconfig.io.model.MappedCharset;
import com.welovecoding.nbeditorconfig.io.reader.FileInfoReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

public abstract class WriteFileAction implements FileSystem.AtomicAction, Runnable {

  private static final Logger LOG = Logger.getLogger(WriteFileAction.class.getName());

  private final FileObject fileObject;
  private final Charset charset;

  public WriteFileAction() {
    fileObject = null;
    charset = null;
  }

  public WriteFileAction(FileObject fileObject, Charset charset) {
    this.fileObject = fileObject;
    this.charset = charset;
  }

  public WriteFileAction(FileObject fileObject) {
    this.fileObject = fileObject;
    MappedCharset mappedCharset = FileInfoReader.readCharset(fileObject);
    this.charset = mappedCharset.getCharset();
  }

  @Override
  public void run() {
    FileLock lock = FileLock.NONE;
    try {
      try (OutputStream outputStream = fileObject.getOutputStream(lock); OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset)) {
        // #####################
        apply(writer);
        // #####################
        setFileAttribute(fileObject, ENCODING_SETTING, charset.name());
        writer.flush();
        outputStream.flush();
      }
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    } finally {
      lock.releaseLock();
    }
  }

  private void setFileAttribute(FileObject fo, String key, String value) {
    try {
      fo.setAttribute(key, value);
    } catch (IOException ex) {
      LOG.log(Level.SEVERE, "Error setting file attribute \"{0}\" with value \"{1}\" for {2}. {3}",
              new Object[]{
                key,
                value,
                fo.getPath(),
                ex.getMessage()
              });
    }
  }

  public abstract void apply(OutputStreamWriter writer);

}
