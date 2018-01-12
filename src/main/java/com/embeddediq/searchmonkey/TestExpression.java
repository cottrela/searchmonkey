/*
 * Copyright (C) 2017 cottr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.embeddediq.searchmonkey;

import com.google.gson.Gson;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.Painter;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;

/**
 *
 * @author cottr
 */
public class TestExpression extends javax.swing.JPanel implements DocumentListener {

    /**
     * Creates new form RegexHelper
     * @param flags
     * @param name
     */
    public TestExpression(int flags, String name) {
        initComponents();
        prefs = Preferences.userNodeForPackage(SearchEntry.class);
        wizardName = name;
        
        Restore(); // Load back previous example content
        
        this.flags = flags;
        
//        try {
//            String fn = "/help/regex.htm";
//            URL url = getClass().getResource(fn);
//            jTextPane1.setPage(url);
//        } catch (IOException ex) {
//            Logger.getLogger(TestExpression.class.getName()).log(Level.SEVERE, null, ex);
//        }

        // Create some styles
        MyStyledDocument doc = new MyStyledDocument();
        try {
            doc.insertString(0, jTextPane2.getText(), doc.nameStyle);
        } catch (BadLocationException ex) {
            Logger.getLogger(TestExpression.class.getName()).log(Level.SEVERE, null, ex);
        }
        jTextPane2.setStyledDocument(doc);

        // Add listener
        // jTextField1.getDocument().addDocumentListener(this);
        // jTextPane2.getDocument().addDocumentListener(this);
        
        //MouseListener popupListener = (MouseListener) new PopupListener2();
        //this.jTextPane2.addMouseListener(popupListener);

    }
    
    private final String def1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla nec orci laoreet mauris venenatis malesuada. Sed vel pretium ex. Aliquam quis metus tristique, cursus augue eu, molestie erat. Praesent eu purus erat. Vestibulum placerat arcu at mi feugiat vulputate. Aenean faucibus libero a lectus iaculis semper. Integer eget ante non eros feugiat volutpat at a tellus. Nulla in sollicitudin tellus, nec tempus odio. Donec sagittis velit sed posuere varius. Duis magna leo, vulputate nec sapien non, efficitur euismod odio. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Suspendisse congue justo quis sapien dignissim, vel pellentesque est gravida.";
    private String wizardName;
    
    private void Restore() // Load back previous example content
    {
        Gson g = new Gson();
        String json = prefs.get(wizardName, g.toJson(def1));
        String item = g.fromJson(json, String.class);
        jTextPane2.setText(item);
    }
    private void RestoreDefaults() // Load back previous example content
    {
        jTextPane2.setText(def1);
    }
    
    public void Save()
    {
        Gson g = new Gson();
        Object val = jTextPane2.getText();
        String json = g.toJson(val);
        prefs.put(wizardName, json); // Add list of look in folders        
    }
    private final Preferences prefs;
    
    public JButton getCloseButton()
    {
        return this.jButton2;
    }
    
    public String getRegex()
    {
        return jTextField1.getText();
    }
    
    public void setRegex(String val)
    {
        jTextField1.setText(val);
    }
    
    private Style as;
    private Style def;
    private int flags;
    private void UpdateRegex()
    {
        int count = 0;
        try
        {
            String txt = jTextField1.getText();
            if (txt.length() == 0) return;
            Pattern compile = Pattern.compile(txt, flags);
            Matcher m = compile.matcher(this.jTextPane2.getText().replaceAll("(?!\\r)\\n", ""));
            MyStyledDocument doc = (MyStyledDocument)this.jTextPane2.getDocument();
            doc.setCharacterAttributes(0, doc.getLength(), doc.nameStyle, true);
            if (m.find())
            {
                this.jTextField1.setBackground(Color.GREEN);
                do{
                    int s = m.start();
                    int e = m.end();
                    doc.setCharacterAttributes(s, e-s, doc.linkStyle, false);
                    count ++;
                    //doc.setParagraphAttributes(s, e-s, as, false);
                } while (m.find());
            }
            else
            {
                this.jTextField1.setBackground(Color.ORANGE);
                this.jTextPane2.setSelectionColor(Color.ORANGE);
            }
        }
        catch (IllegalArgumentException ex)
        {
            this.jTextField1.setBackground(Color.RED);
        }
        // Update the status message
        this.jStatus.setText(String.format("Status: Found %d match%s", count, count == 1 ? "" : "es"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jStatus = new javax.swing.JLabel();

        jMenuItem1.setText("Cut");
        jPopupMenu1.add(jMenuItem1);

        jMenuItem2.setText("Copy");
        jPopupMenu1.add(jMenuItem2);

        jMenuItem3.setText("Paste");
        jPopupMenu1.add(jMenuItem3);
        jPopupMenu1.add(jSeparator1);

        jMenuItem4.setText("jMenuItem4");
        jPopupMenu1.add(jMenuItem4);

        setMaximumSize(new java.awt.Dimension(480, 350));
        setMinimumSize(new java.awt.Dimension(480, 350));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(480, 350));
        setLayout(new java.awt.BorderLayout());

        jScrollPane3.setComponentPopupMenu(jPopupMenu1);

        jTextPane2.setText("<Enter sample text here to test regular expression>");
        jTextPane2.setToolTipText("Enter sample text or copy in from a file to test expression");
        jTextPane2.setInheritsPopupMenu(true);
        jScrollPane3.setViewportView(jTextPane2);

        jLabel3.setText("Sample text:");

        jLabel2.setText("Test expression:");

        jTextField1.setText("sample");
        jTextField1.setToolTipText("Enter test regular expresion");
        jTextField1.setComponentPopupMenu(jPopupMenu1);
        jTextField1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTextField1InputMethodTextChanged(evt);
            }
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setComponentPopupMenu(jPopupMenu1);

        jTextPane1.setEditable(false);
        jTextPane1.setBorder(null);
        jTextPane1.setText("Enter a test expression, and then click Apply to see the results. Searchmonkey will highlight all of the matching text and report the number of hits.\n\nThe sample test can be edited to reflect the type of data you will be searching. Either copy in, or manually enter sample text to test our your regular expression.\n\nAny changes made to this dialog will be saved automatically.");
        jTextPane1.setAutoscrolls(false);
        jTextPane1.setFocusable(false);
        jTextPane1.setOpaque(false);
        jScrollPane1.setViewportView(jTextPane1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jTextField1)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel2, java.awt.BorderLayout.CENTER);

        jButton1.setText("Apply");
        jButton1.setToolTipText("Click apply to test regular expression");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Close");
        jButton2.setToolTipText("Close the dialog");

        jButton3.setText("Reset");
        jButton3.setToolTipText("Reset dialog back to the defaults");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton3)
                    .addComponent(jButton2))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addContainerGap(245, Short.MAX_VALUE))
        );

        add(jPanel3, java.awt.BorderLayout.EAST);

        jStatus.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jStatus.setText("Status:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jStatus)
                .addGap(0, 444, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jStatus)
                .addGap(0, 0, 0))
        );

        add(jPanel1, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTextField1InputMethodTextChanged
        // TODO - add a short delay
        //UpdateRegex();
    }//GEN-LAST:event_jTextField1InputMethodTextChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        UpdateRegex();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.jTextField1.setText("sample");
        this.jTextPane2.setText("<Copy and paste your sample text here>");
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel jStatus;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void insertUpdate(DocumentEvent de) {
        // TODO - add a short delay
        UpdateRegex();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        // TODO - add a short delay
        UpdateRegex();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        // TODO - add a short delay
        UpdateRegex();
    }

    class PopupListener2 implements MouseListener
    {
    
        private void showPopup(MouseEvent e)
        {
            if (e.isPopupTrigger()) {
                jPopupMenu1.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {
            showPopup(me);
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            // if (me.getButton() == MouseEvent.BUTTON2)
            {
                showPopup(me);
            }
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseEntered(MouseEvent me) {
            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseExited(MouseEvent me) {
            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
