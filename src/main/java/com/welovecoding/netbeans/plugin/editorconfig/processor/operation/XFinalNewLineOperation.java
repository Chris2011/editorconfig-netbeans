package com.welovecoding.netbeans.plugin.editorconfig.processor.operation;

import static com.welovecoding.netbeans.plugin.editorconfig.processor.EditorConfigProcessor.OPERATION_LOG_LEVEL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XFinalNewLineOperation {

  private static final Logger LOG = Logger.getLogger(XFinalNewLineOperation.class.getSimpleName());

  static {
    LOG.setLevel(OPERATION_LOG_LEVEL);
  }

  public static boolean doFinalNewLine(StringBuilder content, final String finalNewLine, final String lineEnding) {

    return new XFinalNewLineOperation().apply(content, finalNewLine, lineEnding);
  }

  public boolean apply(StringBuilder content, final String finalNewLine, final String lineEnding) {
    boolean newline = Boolean.valueOf(finalNewLine);
    boolean changed = false;

    LOG.log(Level.INFO, "Executing ApplyTestTask");

    if (newline) {
      LOG.log(Level.INFO, "INSERT_FINAL_NEWLINE = true");
      String tempContent = content.toString();
      LOG.log(Level.INFO, "OLDCONTENT: {0}.", tempContent);
      content = addFinalNewLine(content, lineEnding);

      if (tempContent.equals(content.toString())) {
        LOG.log(Level.INFO, "INSERT_FINAL_NEWLINE : No changes");
        changed = false;
      } else {
        LOG.log(Level.INFO, "INSERT_FINAL_NEWLINE : appended final new line");
        changed = true;
      }
      LOG.log(Level.INFO, "NEWCONTENT: {0}.", content);
    }

    return changed;
  }

  private StringBuilder addFinalNewLine(StringBuilder content, String lineEnding) {
    if (!content.toString().endsWith("\n") && !content.toString().endsWith("\r")) {
      LOG.log(Level.INFO, "INSERT_FINAL_NEWLINE : Adding final newline");
      return content.append(lineEnding);
    } else {
      LOG.log(Level.INFO, "INSERT_FINAL_NEWLINE : No changes");
      return content;
    }
  }
}
