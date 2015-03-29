package com.welovecoding.nbeditorconfig.filetype;

import static com.welovecoding.nbeditorconfig.config.Settings.MIME_TYPE;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = MIMEResolver.class, position = 3214328)
public class FilenameResolver extends MIMEResolver {

  private static final Logger LOG = Logger.getLogger(FilenameResolver.class.getName());

  public FilenameResolver() {
    super(MIME_TYPE);
  }

  @Override
  public String findMIMEType(FileObject fo) {
    String nameExt = fo.getNameExt();

    if (".editorconfig".equalsIgnoreCase(nameExt)) {
      return MIME_TYPE;
    }

    return null;
  }

}
