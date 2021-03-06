/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * RootSelectorPanel.java
 *
 * Created on Dec 1, 2008, 2:41:58 PM
 */

package org.netbeans.modules.versioning.util;

import java.awt.Dialog;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tomas Stuka
 */
public class FileSelector extends javax.swing.JPanel implements ListSelectionListener {
    private DialogDescriptor dialogDescriptor;
    private JButton okButton;
    private JButton cancelButton;
    private final String text;
    private final String title;
    private final HelpCtx helpCtx;
    private final Preferences pref;
    private static final String FILE_SELECTOR_PREFIX = "fileSelector";                               // NOI18N
    private static final Logger LOG = Logger.getLogger(FileSelector.class.getName());

    /** Creates new form RootSelectorPanel */
    public FileSelector(String text, String title, HelpCtx helpCtx, Preferences pref) {
        this.text = text;
        this.title = title;
        this.helpCtx = helpCtx;
        this.pref = pref;

        initComponents();

        filesList.addListSelectionListener(this);

        dialogDescriptor = new DialogDescriptor(this, title);

        okButton = new JButton(org.openide.util.NbBundle.getMessage(FileSelector.class, "CTL_FileSelector_Select"));
        okButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSelector.class, "CTL_FileSelector_Select"));
        okButton.setEnabled(false);
        cancelButton = new JButton(org.openide.util.NbBundle.getMessage(FileSelector.class, "CTL_FileSelector_Cancel"));                                      // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSelector.class, "CTL_FileSelector_Cancel"));    // NOI18N
        dialogDescriptor.setOptions(new Object[] {okButton, cancelButton});

        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(new HelpCtx(this.getClass()));
        dialogDescriptor.setValid(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();

        jLabel1.setText(text);

        filesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        filesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(filesList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 606, Short.MAX_VALUE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JList filesList = new javax.swing.JList();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    public boolean show(File[] files) {
        Arrays.sort(files);
        DefaultListModel<File> m = new DefaultListModel<>();
        for (File file : files) {
            m.addElement(file);
        }
        filesList.setModel(m);
        preselectFile(files);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(title);                     // NOI18N

        dialogDescriptor.setHelpCtx(helpCtx);
        dialog.setVisible(true);
        boolean ret = dialogDescriptor.getValue() == okButton;
        if(ret) {
            saveSelectedFile(files);
        }
        return ret;
    }

    public File getSelectedFile() {
        return (File) filesList.getSelectedValue();
    }

    public void valueChanged(ListSelectionEvent e) {
        boolean enabled = filesList.getSelectedValue() != null;
        dialogDescriptor.setValid(enabled);
        okButton.setEnabled(enabled);
    }

    private void preselectFile(File[] files) {
        String hash = getHash(files);
        if(hash == null || hash.trim().equals("")) {
            return;
        }
        String path = getFileSelectorPreset(hash);
        if(path != null && !path.trim().equals("")) {
            File f = new File(path);
            filesList.setSelectedValue(f, true);
        }
    }

    private void saveSelectedFile(File[] files) {
        String hash = getHash(files);
        if(hash == null || hash.trim().equals("")) {
            return;
        }
        File file = getSelectedFile();
        if(file != null) {
            setFileSelectorPreset(hash, file.getAbsolutePath());
        }
    }

    private String getHash(File[] files) {
        Arrays.sort(files);
        StringBuffer sb = new StringBuffer();
        for (File file : files) {
            sb.append(file.getAbsolutePath());
        }
        String hash = null;
        try {
            hash = Utils.getHash("MD5", sb.toString().getBytes());
        } catch (NoSuchAlgorithmException ex) {
            LOG.log(Level.SEVERE, null, ex); // should not happen
        }
        return hash;
    }

    public String getFileSelectorPreset(String hash) {
        return pref.get(FILE_SELECTOR_PREFIX + "-" + hash, "");
    }

    public void setFileSelectorPreset(String hash, String path) {
        pref.put(FILE_SELECTOR_PREFIX + "-" + hash, path);
    }


}
