package com.welovecoding.nbeditorconfig.options;

import javax.swing.ComboBoxModel;

final class EditorConfigOptionsPanel extends javax.swing.JPanel {

  private static final long serialVersionUID = 6812836508405537948L;

  private final EditorConfigOptionsPanelController controller;

  EditorConfigOptionsPanel(EditorConfigOptionsPanelController controller) {
    this.controller = controller;
    initComponents();
    // TODO listen to changes in form fields and call controller.changed()
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    lineCommentPrefixComboBox = new javax.swing.JComboBox<String>();
    lineCommnetPrefixLabel = new javax.swing.JLabel();

    lineCommentPrefixComboBox.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "#", ";" }));

    org.openide.awt.Mnemonics.setLocalizedText(lineCommnetPrefixLabel, org.openide.util.NbBundle.getMessage(EditorConfigOptionsPanel.class, "EditorConfigOptionsPanel.lineCommnetPrefixLabel.text")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lineCommnetPrefixLabel)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(lineCommentPrefixComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(lineCommentPrefixComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lineCommnetPrefixLabel))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  void load() {
    EditorConfigOptions options = EditorConfigOptions.getInstance();
    lineCommentPrefixComboBox.setSelectedItem(options.getLineCommentPrefix());
  }

  void store() {
    EditorConfigOptions options = EditorConfigOptions.getInstance();
    options.setLineCommentPrefix(getLineCommentPrefix());
  }

  private String getLineCommentPrefix() {
    return (String) lineCommentPrefixComboBox.getSelectedItem();
  }

  boolean valid() {
    // TODO check whether form is consistent and complete
    return true;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox<String> lineCommentPrefixComboBox;
  private javax.swing.JLabel lineCommnetPrefixLabel;
  // End of variables declaration//GEN-END:variables
}
