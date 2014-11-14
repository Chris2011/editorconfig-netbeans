package com.welovecoding.netbeans.plugin.editorconfig.processor;

import com.welovecoding.netbeans.plugin.editorconfig.mapper.EditorConfigPropertyMapper;
import com.welovecoding.netbeans.plugin.editorconfig.model.EditorConfigConstant;
import com.welovecoding.netbeans.plugin.editorconfig.processor.operation.IndentSizeOperation;
import com.welovecoding.netbeans.plugin.editorconfig.processor.operation.IndentStyleOperation;
import com.welovecoding.netbeans.plugin.editorconfig.processor.operation.XFinalNewLineOperation;
import com.welovecoding.netbeans.plugin.editorconfig.processor.operation.XLineEndingOperation;
import com.welovecoding.netbeans.plugin.editorconfig.processor.operation.XTabWidthOperation;
import com.welovecoding.netbeans.plugin.editorconfig.processor.operation.XTrimTrailingWhitespacesOperation;
import com.welovecoding.netbeans.plugin.editorconfig.util.NetBeansFileUtil;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.editorconfig.core.EditorConfig;
import org.editorconfig.core.EditorConfigException;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Michael Koppen
 */
public class EditorConfigProcessor {

  private static final Logger LOG = Logger.getLogger(EditorConfigProcessor.class.getName());
  private final EditorConfig ec;

  public EditorConfigProcessor() {
    ec = new EditorConfig(".editorconfig", EditorConfig.VERSION);
  }

  private HashMap<String, String> parseRulesForFile(DataObject dataObject) {
    String filePath = dataObject.getPrimaryFile().getPath();

    LOG.log(Level.INFO, "Apply rules for: {0}", filePath);

    List<EditorConfig.OutPair> rules = new ArrayList<>();

    try {
      rules = ec.getProperties(filePath);
    } catch (EditorConfigException ex) {
      LOG.log(Level.SEVERE, ex.getMessage());
    }

    HashMap<String, String> keyedRules = new HashMap<>();
    for (EditorConfig.OutPair rule : rules) {
      keyedRules.put(rule.getKey().toLowerCase(), rule.getVal().toLowerCase());
    }
    return keyedRules;
  }

  /**
   * Applies EditorConfig rules for the given file.
   *
   * @param dataObject
   */
  public void applyRulesToFile(DataObject dataObject) throws Exception {

    HashMap<String, String> keyedRules = parseRulesForFile(dataObject);

    FileObject fileObject = dataObject.getPrimaryFile();

    // Save file before appling any changes when opened in editor
    EditorCookie cookie = getEditorCookie(fileObject);
    boolean isOpenedInEditor = cookie != null && cookie.getDocument() != null;

    if (isOpenedInEditor) {
      LOG.log(Level.INFO, "File is opened in Editor! Saving all changes.");
      StyledDocument document = cookie.getDocument();
      NbDocument.runAtomicAsUser(document, () -> {
        try {
          cookie.saveDocument();
        } catch (IOException ex) {
          Exceptions.printStackTrace(ex);
        }
      });
    }
    StringBuilder content = new StringBuilder(fileObject.asText());
    boolean changed = false;
    boolean charsetChange = false;
    boolean styleChanged = false;

    for (Map.Entry<String, String> rule : keyedRules.entrySet()) {
      final String key = rule.getKey();
      final String value = rule.getValue();

      LOG.log(Level.INFO, "Found rule \"{1}\" with value \"{2}\".", new Object[]{key, value});

      switch (key) {
        case EditorConfigConstant.CHARSET:
          Charset currentCharset = NetBeansFileUtil.guessCharset(fileObject);
          Charset requestedCharset = EditorConfigPropertyMapper.mapCharset(keyedRules.get(EditorConfigConstant.CHARSET));
          if (!currentCharset.equals(requestedCharset)) {
            charsetChange = true;
          }
          break;
        case EditorConfigConstant.END_OF_LINE:
          changed = XLineEndingOperation.doChangeLineEndings(
                  content,
                  EditorConfigPropertyMapper.normalizeLineEnding(keyedRules.get(EditorConfigConstant.END_OF_LINE))) || changed;
          break;
        case EditorConfigConstant.INDENT_SIZE:
          //TODO this should happen in the file!!
          styleChanged = IndentSizeOperation.doIndentSize(dataObject, value) || styleChanged;
          break;
        case EditorConfigConstant.INDENT_STYLE:
          //TODO this happens in the file!!
          styleChanged = IndentStyleOperation.doIndentStyle(dataObject, key) || styleChanged;
          break;
        case EditorConfigConstant.INSERT_FINAL_NEWLINE:
          changed = XFinalNewLineOperation.doFinalNewLine(
                  content,
                  value,
                  EditorConfigPropertyMapper.normalizeLineEnding(keyedRules.get(EditorConfigConstant.END_OF_LINE))) || changed;
          break;
        case EditorConfigConstant.TAB_WIDTH:
          styleChanged = XTabWidthOperation.doTabWidth(dataObject, value) || styleChanged;
          break;
        case EditorConfigConstant.TRIM_TRAILING_WHITESPACE:
          changed = XTrimTrailingWhitespacesOperation.doTrimTrailingWhitespaces(
                  content,
                  value,
                  EditorConfigPropertyMapper.normalizeLineEnding(keyedRules.get(EditorConfigConstant.END_OF_LINE))) || changed;
          break;
        default:
          LOG.log(Level.WARNING, "Unknown property: {0}", key);
          break;
      }
    }

    flushFile(
            fileObject,
            content,
            changed,
            charsetChange,
            EditorConfigPropertyMapper.mapCharset(keyedRules.get(EditorConfigConstant.CHARSET)),
            isOpenedInEditor,
            cookie);

    flushStyles(fileObject, styleChanged);

  }

  private void flushFile(FileObject fileObject, StringBuilder content, boolean changed, boolean charsetChange, Charset charset, boolean flushInEditor, EditorCookie cookie) throws BadLocationException {
    if (changed || charsetChange) {
      new WriteFileTask(fileObject, charset) {
        @Override
        public void apply(OutputStreamWriter writer) {
          try {
            writer.write(content.toString());
          } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
          }
        }
      }.run();
      if (flushInEditor) {
        LOG.log(Level.INFO, "Update changes in Editor window");
        StyledDocument document = cookie.getDocument();
        NbDocument.runAtomicAsUser(document, () -> {
          try {
            //TODO This is a workaround to update the content of an currently opened editor window
            document.remove(0, document.getLength());
            document.insertString(0, fileObject.asText(), null);
            cookie.saveDocument();
          } catch (BadLocationException | IOException ex) {
            Exceptions.printStackTrace(ex);
          }
        });
      }
    }
  }

  private void flushStyles(FileObject fileObject, boolean styleChanged) {
    if (styleChanged) {
      try {
        Preferences codeStyle = CodeStylePreferences.get(fileObject, fileObject.getMIMEType()).getPreferences();
        codeStyle.flush();
      } catch (BackingStoreException ex) {
        LOG.log(Level.SEVERE, "Error applying code style: {0}", ex.getMessage());
      }
    }
  }

  private EditorCookie getEditorCookie(FileObject fileObject) {
    try {
      return (EditorCookie) DataObject.find(fileObject).getLookup().lookup(EditorCookie.class);
    } catch (DataObjectNotFoundException ex) {
      Exceptions.printStackTrace(ex);
      return null;
    }
  }
}
