/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author cottr
 */
public class SearchEntryPanel extends javax.swing.JPanel implements ChangeListener {

    JSpinner popup_link;
    PopupCalendar cal;
    /**
     * Creates new form SearchEntryPanel
     */
    public SearchEntryPanel() {
        initComponents();
        
        cal = new PopupCalendar();
        cal.getCalendar().addChangeListener(this);

        jAfter.addMouseListener(new MyMouseAdapter(jAfter, jAfterSpinner));
        jBefore.addMouseListener(new MyMouseAdapter(jBefore, jBeforeSpinner));
        jAfter1.addMouseListener(new MyMouseAdapter(jAfter1, jAfterSpinner1));
        jBefore1.addMouseListener(new MyMouseAdapter(jBefore1, jBeforeSpinner1));
        jAfter2.addMouseListener(new MyMouseAdapter(jAfter2, jAfterSpinner2));
        jBefore2.addMouseListener(new MyMouseAdapter(jBefore2, jBeforeSpinner2));

        // Restore the settings
        Restore();
    }
    
    public class PopupCalendar extends JPopupMenu {
        CalendarPopup panel;
        public PopupCalendar (){
            panel = new CalendarPopup();
            
            this.add(panel);
            this.pack();
        }
        
        public CalendarPopup getCalendar()
        {
            return panel;
        }
        
        private JSpinner popup_link;
        public void show(JButton jButton, JSpinner link)
        {
            popup_link = link;
            panel.setDate((Date)link.getValue());
            this.show(jButton, 0, jButton.getHeight());
        }
        public void updateDate()
        {
            if (popup_link == null) return;
            popup_link.setValue(panel.getDate());
        }
    }

    @Override
    public void stateChanged(ChangeEvent ce) {
        cal.updateDate();
    }
    
    public class MyMouseAdapter extends MouseAdapter {
        JButton jButton;
        JSpinner link;
        public MyMouseAdapter(JButton jButton, JSpinner link)
        {
           //this.cal = cal;
            this.jButton = jButton;
            this.link = link;
        }
        
        @Override
        public void mousePressed(MouseEvent e)
        {
            cal.show(jButton, link);
        }
    }
   
    public SearchEntry getSearchRequest() {
        SearchEntry req = new SearchEntry();
        String strItem;

        // Get look in folder
        req.lookIn = new ArrayList<>();
        Object folder = jLookIn.getSelectedItem();
        if (folder.getClass().equals(String.class))
        {
            strItem = (String)folder;
            req.lookIn.add(Paths.get(strItem));
        }
        else if (folder.getClass().equals(List.class))
        { // Check for list
            for (String item: (List<String>)folder)
            {
                req.lookIn.add(Paths.get(item));
            }
        } else { // Unsupported
            System.out.println("Error! Unsupported class type..");
        }
        req.lookInSubFolders = jSubFolders.isSelected();
        
        // Get filename folder
        strItem = (String)jFileName.getSelectedItem();
        String prefix = (jUseFileRegex.isSelected() ? SearchEntry.PREFIX_REGEX : SearchEntry.PREFIX_GLOB);
        req.fileName = FileSystems.getDefault().getPathMatcher(prefix + strItem);
        
        // Get containing text
        if (jContainingText.getSelectedItem() != null)
        {
            strItem = (String)jContainingText.getSelectedItem();
            if (strItem.length() > 0) // Is there a content match to make?
            {
                Pattern regex = Pattern.compile(strItem);
                req.containingText = new ContentMatch(regex);
            }
        }
        
        // Get min/max size
        double scaler = Math.pow(1024,jFileSizeScaler.getSelectedIndex()); // 1024^0 = 1; 1024^1=1K, 1024^2=1M, etc
        if (jLessThanToggle.isSelected()) {
            req.lessThan = (long)(scaler * (double)jLessThanSpinner.getValue());
        }
        if (jGreaterThanToggle.isSelected()) {
            req.greaterThan = (long)(scaler * (double)jGreaterThanSpinner.getValue());
        }

        // Get modifed before/after date
        if (jAfterToggle.isSelected()) {
            Date d = ((SpinnerDateModel)jAfterSpinner.getModel()).getDate();
            req.modifiedAfter = FileTime.from(d.toInstant());
        }
        if (jBeforeToggle.isSelected()) {
            Date d = ((SpinnerDateModel)jBeforeSpinner.getModel()).getDate();
            req.modifiedBefore = FileTime.from(d.toInstant());
        }

        // Set flags from the options tab
        req.flags.useFilenameRegex = jUseFileRegex.isSelected();
        req.flags.useContentRegex = jUseContentRegex.isSelected();
        req.flags.ignoreHiddenFiles = jIgnoreHiddenFiles.isSelected();
        req.flags.ignoreHiddenFolders = jIgnoreHiddenFolders.isSelected();
        req.flags.ignoreHiddenFiles = jIgnoreHiddenFiles.isSelected();
        req.flags.ignoreSymbolicLinks = jIgnoreSymbolicLinks.isSelected();
        req.flags.lookInSubFolders = jSubFolders.isSelected();

        // Get created before/after date
        if (jAfterToggle1.isSelected()) {
            Date d = ((SpinnerDateModel)jAfterSpinner1.getModel()).getDate();
            req.createdAfter = FileTime.from(d.toInstant());
        }
        if (jBeforeToggle1.isSelected()) {
            Date d = ((SpinnerDateModel)jBeforeSpinner1.getModel()).getDate();
            req.createdBefore = FileTime.from(d.toInstant());
        }

        // Get accessed before/after date
        if (jAfterToggle2.isSelected()) {
            Date d = ((SpinnerDateModel)jAfterSpinner2.getModel()).getDate();
            req.accessedAfter = FileTime.from(d.toInstant());
        }
        if (jBeforeToggle2.isSelected()) {
            Date d = ((SpinnerDateModel)jBeforeSpinner2.getModel()).getDate();
            req.accessedBefore = FileTime.from(d.toInstant());
        }
        return req;
    }
    
    public void Save()
    {
        Preferences prefs = Preferences.userNodeForPackage(SearchEntry.class);
        String json;
        
        Gson g = new Gson();
        List<String> items = new ArrayList<>();
        for (int i=0; i<jLookIn.getItemCount(); i++)
        {
            items.add(jLookIn.getItemAt(i));
        }
        json = g.toJson(items);
        prefs.put("LookIn", json); // Add list of look in folders
        
        prefs.putBoolean("LookInSubFolders", jSubFolders.isSelected());
    }
    
    public void Restore()
    {
        Preferences prefs = Preferences.userNodeForPackage(SearchEntry.class);
        String json;
        
        Gson g = new Gson();
        json = prefs.get("LookIn", "['Hello', 'World']"); // Add list of look in folders
        List<String> items;
        // items = g.fromJson(json, new ArrayList<String>(){}.getClass());
        items = g.fromJson(json, new TypeToken<ArrayList<String>>() {}.getType());
        
        jLookIn.removeAllItems(); // Clear the list
        for (String item: items)
        {
            jLookIn.addItem(item);
        }
    }
    
    /**
    * this class was created by two ibm authors.
    * @see http://www.ibm.com/developerworks/web/library/us-j2d/
    */
    public class RolloverIcon extends ImageIcon
    {
        /**
         * Generated SUID
         */
        private static final long serialVersionUID = 3757470229899737051L;
        protected ImageIcon fIcon;

        /**
         * Construct us with the icon we will create paint a rollover icon for
         * @param component
         * @param anIcon
         */
        public RolloverIcon(Component component, ImageIcon anIcon) {
            super();
            int width = anIcon.getImage().getWidth(null);
            int height = anIcon.getImage().getHeight(null);
            BufferedImage bufferedImage = new BufferedImage(width ,height, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g2D = (Graphics2D) bufferedImage.getGraphics();
            g2D.setComposite(RolloverComposite.getInstance());
            anIcon.paintIcon(component, g2D, 0, 0);
            setImage(bufferedImage);
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        FilenameSearchType = new javax.swing.ButtonGroup();
        ContentSearchType = new javax.swing.ButtonGroup();
        jFileChooser1 = new javax.swing.JFileChooser();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jModifiedPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jBeforeToggle = new javax.swing.JToggleButton();
        jBeforeSpinner = new javax.swing.JSpinner();
        jBefore = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jAfterToggle = new javax.swing.JToggleButton();
        jAfterSpinner = new javax.swing.JSpinner();
        jAfter = new javax.swing.JButton();
        jLookInPanel = new javax.swing.JPanel();
        jSubFolders = new javax.swing.JCheckBox();
        jLookIn = new javax.swing.JComboBox<>();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLessThanToggle = new javax.swing.JToggleButton();
        jGreaterThanSpinner = new javax.swing.JSpinner();
        jGreaterThanToggle = new javax.swing.JToggleButton();
        jLessThanSpinner = new javax.swing.JSpinner();
        jFileSizeScaler = new javax.swing.JComboBox<>();
        jPanel6 = new javax.swing.JPanel();
        jContainingText = new javax.swing.JComboBox<>();
        jButton8 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jFileName = new javax.swing.JComboBox<>();
        jButton4 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jUseFileGlobs = new javax.swing.JRadioButton();
        jUseFileRegex = new javax.swing.JRadioButton();
        jUseContentSearch = new javax.swing.JRadioButton();
        jUseContentRegex = new javax.swing.JRadioButton();
        jIgnoreHiddenFiles = new javax.swing.JCheckBox();
        jIgnoreHiddenFolders = new javax.swing.JCheckBox();
        jIgnoreSymbolicLinks = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jModifiedPanel1 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jBeforeToggle1 = new javax.swing.JToggleButton();
        jBeforeSpinner1 = new javax.swing.JSpinner();
        jBefore1 = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        jAfterToggle1 = new javax.swing.JToggleButton();
        jAfterSpinner1 = new javax.swing.JSpinner();
        jAfter1 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jModifiedPanel2 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jBeforeToggle2 = new javax.swing.JToggleButton();
        jBeforeSpinner2 = new javax.swing.JSpinner();
        jBefore2 = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jAfterToggle2 = new javax.swing.JToggleButton();
        jAfterSpinner2 = new javax.swing.JSpinner();
        jAfter2 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();

        jFileChooser1.setApproveButtonText("OK");
        jFileChooser1.setApproveButtonToolTipText("Click to select base folder");
        jFileChooser1.setDialogTitle("Select folder to look in");
        jFileChooser1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        jFileChooser1.setToolTipText("");

        jLabel1.setLabelFor(jFileName);
        jLabel1.setText("File name:");

        jLabel3.setLabelFor(jContainingText);
        jLabel3.setText("Containing Text:");

        jLabel4.setLabelFor(jLookInPanel);
        jLabel4.setText("Look in:");
        jLabel4.setToolTipText("");

        jLabel5.setLabelFor(jModifiedPanel);
        jLabel5.setText("Modified:");

        jBeforeToggle.setText("Before:");
        jBeforeToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jBeforeToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBeforeToggleActionPerformed(evt);
            }
        });

        jBeforeSpinner.setModel(new javax.swing.SpinnerDateModel());
        jBeforeSpinner.setEnabled(false);

        jBefore.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/calendar.png"))); // NOI18N
        jBefore.setBorder(null);
        jBefore.setBorderPainted(false);
        jBefore.setContentAreaFilled(false);
        jBefore.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jBeforeToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBeforeSpinner)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBefore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBeforeToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBefore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBeforeSpinner))
                .addContainerGap())
        );

        jAfterToggle.setText("After:");
        jAfterToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jAfterToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAfterToggleActionPerformed(evt);
            }
        });

        jAfterSpinner.setModel(new javax.swing.SpinnerDateModel());
        jAfterSpinner.setEnabled(false);

        jAfter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/calendar.png"))); // NOI18N
        jAfter.setBorder(null);
        jAfter.setBorderPainted(false);
        jAfter.setContentAreaFilled(false);
        jAfter.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jAfterToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jAfterSpinner)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAfter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAfterToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAfterSpinner)
                    .addComponent(jAfter, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jModifiedPanelLayout = new javax.swing.GroupLayout(jModifiedPanel);
        jModifiedPanel.setLayout(jModifiedPanelLayout);
        jModifiedPanelLayout.setHorizontalGroup(
            jModifiedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jModifiedPanelLayout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jModifiedPanelLayout.setVerticalGroup(
            jModifiedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jModifiedPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jModifiedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        jSubFolders.setSelected(true);
        jSubFolders.setText("Sub-folders");

        jLookIn.setEditable(true);
        jLookIn.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jLookIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLookInActionPerformed(evt);
            }
        });

        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/folder-150113_640.png"))); // NOI18N
        jButton9.setBorder(null);
        jButton9.setBorderPainted(false);
        jButton9.setContentAreaFilled(false);
        jButton9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/folder-tree.png"))); // NOI18N
        jButton10.setBorder(null);
        jButton10.setBorderPainted(false);
        jButton10.setContentAreaFilled(false);
        jButton10.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jLookInPanelLayout = new javax.swing.GroupLayout(jLookInPanel);
        jLookInPanel.setLayout(jLookInPanelLayout);
        jLookInPanelLayout.setHorizontalGroup(
            jLookInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLookInPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jLookIn, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSubFolders)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10)
                .addContainerGap())
        );
        jLookInPanelLayout.setVerticalGroup(
            jLookInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jLookInPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jLookInPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLookIn, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSubFolders, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        jLessThanToggle.setText("≤");
        jLessThanToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jLessThanToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jLessThanToggleActionPerformed(evt);
            }
        });

        jGreaterThanSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));
        jGreaterThanSpinner.setEnabled(false);

        jGreaterThanToggle.setText("≥");
        jGreaterThanToggle.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jGreaterThanToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jGreaterThanToggleActionPerformed(evt);
            }
        });

        jLessThanSpinner.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, null, 1.0d));
        jLessThanSpinner.setEnabled(false);

        jFileSizeScaler.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Size (Bytes):", "Size (KBytes):", "Size (MBytes):", "Size (GBytes):", "Size (TBytes):" }));
        jFileSizeScaler.setSelectedIndex(1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jFileSizeScaler, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jGreaterThanToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jGreaterThanSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLessThanToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLessThanSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jFileSizeScaler)
                            .addComponent(jGreaterThanSpinner)
                            .addComponent(jLessThanSpinner)))
                    .addComponent(jGreaterThanToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLessThanToggle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jContainingText.setEditable(true);
        jContainingText.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help.png"))); // NOI18N
        jButton8.setBorder(null);
        jButton8.setBorderPainted(false);
        jButton8.setContentAreaFilled(false);
        jButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/074082-rounded-glossy-black-icon-alphanumeric-font-size.png"))); // NOI18N
        jButton5.setBorder(null);
        jButton5.setBorderPainted(false);
        jButton5.setContentAreaFilled(false);
        jButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jContainingText, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jContainingText, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help.png"))); // NOI18N
        jButton7.setBorder(null);
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jFileName.setEditable(true);
        jFileName.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/074082-rounded-glossy-black-icon-alphanumeric-font-size.png"))); // NOI18N
        jButton4.setBorder(null);
        jButton4.setBorderPainted(false);
        jButton4.setContentAreaFilled(false);
        jButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jFileName, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jModifiedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLookInPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLookInPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jModifiedPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Main", jPanel1);

        FilenameSearchType.add(jUseFileGlobs);
        jUseFileGlobs.setSelected(true);
        jUseFileGlobs.setText("Search filenames using 'glob' expressions e.g. *.txt");

        FilenameSearchType.add(jUseFileRegex);
        jUseFileRegex.setText("Search filenames using regular expressions");

        ContentSearchType.add(jUseContentSearch);
        jUseContentSearch.setSelected(true);
        jUseContentSearch.setText("Search file content using key words");

        ContentSearchType.add(jUseContentRegex);
        jUseContentRegex.setText("Search file content using regular expressions");

        jIgnoreHiddenFiles.setText("Ignore hidden files");

        jIgnoreHiddenFolders.setText("Ignore hidden folders");

        jIgnoreSymbolicLinks.setText("Ignore symbolic links and shortcuts");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jUseFileRegex)
                    .addComponent(jUseFileGlobs)
                    .addComponent(jIgnoreHiddenFiles))
                .addGap(121, 121, 121)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jIgnoreHiddenFolders)
                    .addComponent(jUseContentRegex)
                    .addComponent(jUseContentSearch)
                    .addComponent(jIgnoreSymbolicLinks))
                .addContainerGap(103, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jUseFileRegex)
                    .addComponent(jUseContentRegex))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jUseContentSearch)
                    .addComponent(jUseFileGlobs))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jIgnoreHiddenFiles)
                    .addComponent(jIgnoreHiddenFolders))
                .addGap(23, 23, 23)
                .addComponent(jIgnoreSymbolicLinks)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Options", jPanel8);

        jLabel6.setLabelFor(jModifiedPanel);
        jLabel6.setText("Created:");

        jBeforeToggle1.setText("Before:");
        jBeforeToggle1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jBeforeToggle1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBeforeToggle1ActionPerformed(evt);
            }
        });

        jBeforeSpinner1.setModel(new javax.swing.SpinnerDateModel());
        jBeforeSpinner1.setEnabled(false);

        jBefore1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/calendar.png"))); // NOI18N
        jBefore1.setBorder(null);
        jBefore1.setBorderPainted(false);
        jBefore1.setContentAreaFilled(false);
        jBefore1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jBeforeToggle1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBeforeSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBefore1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBeforeToggle1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jBefore1)
                            .addComponent(jBeforeSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jAfterToggle1.setText("After:");
        jAfterToggle1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jAfterToggle1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAfterToggle1ActionPerformed(evt);
            }
        });

        jAfterSpinner1.setModel(new javax.swing.SpinnerDateModel());
        jAfterSpinner1.setEnabled(false);

        jAfter1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/calendar.png"))); // NOI18N
        jAfter1.setBorder(null);
        jAfter1.setBorderPainted(false);
        jAfter1.setContentAreaFilled(false);
        jAfter1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jAfterToggle1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jAfterSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAfter1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAfter1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAfterToggle1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAfterSpinner1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jModifiedPanel1Layout = new javax.swing.GroupLayout(jModifiedPanel1);
        jModifiedPanel1.setLayout(jModifiedPanel1Layout);
        jModifiedPanel1Layout.setHorizontalGroup(
            jModifiedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jModifiedPanel1Layout.createSequentialGroup()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jModifiedPanel1Layout.setVerticalGroup(
            jModifiedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jModifiedPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jModifiedPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        jLabel7.setLabelFor(jModifiedPanel);
        jLabel7.setText("Accessed:");

        jBeforeToggle2.setText("Before:");
        jBeforeToggle2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jBeforeToggle2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBeforeToggle2ActionPerformed(evt);
            }
        });

        jBeforeSpinner2.setModel(new javax.swing.SpinnerDateModel());
        jBeforeSpinner2.setEnabled(false);

        jBefore2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/calendar.png"))); // NOI18N
        jBefore2.setBorder(null);
        jBefore2.setBorderPainted(false);
        jBefore2.setContentAreaFilled(false);
        jBefore2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addComponent(jBeforeToggle2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jBeforeSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jBefore2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jBeforeToggle2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jBefore2)
                            .addComponent(jBeforeSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jAfterToggle2.setText("After:");
        jAfterToggle2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jAfterToggle2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAfterToggle2ActionPerformed(evt);
            }
        });

        jAfterSpinner2.setModel(new javax.swing.SpinnerDateModel());
        jAfterSpinner2.setEnabled(false);

        jAfter2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/calendar.png"))); // NOI18N
        jAfter2.setBorder(null);
        jAfter2.setBorderPainted(false);
        jAfter2.setContentAreaFilled(false);
        jAfter2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jAfterToggle2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jAfterSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jAfter2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jAfter2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAfterToggle2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jAfterSpinner2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jModifiedPanel2Layout = new javax.swing.GroupLayout(jModifiedPanel2);
        jModifiedPanel2.setLayout(jModifiedPanel2Layout);
        jModifiedPanel2Layout.setHorizontalGroup(
            jModifiedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jModifiedPanel2Layout.createSequentialGroup()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jModifiedPanel2Layout.setVerticalGroup(
            jModifiedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jModifiedPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jModifiedPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jModifiedPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jModifiedPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(342, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jModifiedPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jModifiedPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(82, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Advanced", jPanel9);

        jButton1.setText("Start");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Stop");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Expert");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2)
                    .addComponent(jButton1)
                    .addComponent(jCheckBox1))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addGap(43, 43, 43)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jLookInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLookInActionPerformed
    }//GEN-LAST:event_jLookInActionPerformed
    
    private Collection<ActionListener> listeners = new LinkedList<>();
    public void addActionListener(ActionListener listener)
    {
        listeners.add(listener);
    }
    
//    private Searchmonkey parent;
//    public void setParent(Searchmonkey parent)
//    {
//        this.parent = parent;
//    }
    
    public void Start()
    {
        Save();
        
        // Call the parent
        jButton1.setEnabled(false);
        jButton2.setEnabled(true);
    }
    public void Stop()
    {
        jButton2.setEnabled(false);
        jButton1.setEnabled(true);
    }
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        for(ActionListener listener: listeners){
            ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Start");
            listener.actionPerformed(ae);
           //  listener.stateChanged(new ChangeEvent(this));
        } 
        //Start();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        for(ActionListener listener: listeners){
            ActionEvent ae = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Stop");
            listener.actionPerformed(ae);
           //  listener.stateChanged(new ChangeEvent(this));
        }
        //Stop();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jGreaterThanToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGreaterThanToggleActionPerformed
        jGreaterThanSpinner.setEnabled(jGreaterThanToggle.isSelected());
    }//GEN-LAST:event_jGreaterThanToggleActionPerformed

    private void jLessThanToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLessThanToggleActionPerformed
        jLessThanSpinner.setEnabled(jLessThanToggle.isSelected());
    }//GEN-LAST:event_jLessThanToggleActionPerformed

    private void jAfterToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAfterToggleActionPerformed
        jAfterSpinner.setEnabled(jAfterToggle.isSelected());
    }//GEN-LAST:event_jAfterToggleActionPerformed

    private void jBeforeToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBeforeToggleActionPerformed
        jBeforeSpinner.setEnabled(jBeforeToggle.isSelected());
    }//GEN-LAST:event_jBeforeToggleActionPerformed

    private void jAfterToggle1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAfterToggle1ActionPerformed
        jAfterSpinner1.setEnabled(jAfterToggle1.isSelected());
    }//GEN-LAST:event_jAfterToggle1ActionPerformed

    private void jAfterToggle2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAfterToggle2ActionPerformed
        jAfterSpinner2.setEnabled(jAfterToggle2.isSelected());
    }//GEN-LAST:event_jAfterToggle2ActionPerformed

    private void jBeforeToggle1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBeforeToggle1ActionPerformed
        jBeforeSpinner1.setEnabled(jBeforeToggle1.isSelected());
    }//GEN-LAST:event_jBeforeToggle1ActionPerformed

    private void jBeforeToggle2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBeforeToggle2ActionPerformed
        jBeforeSpinner2.setEnabled(jBeforeToggle2.isSelected());
    }//GEN-LAST:event_jBeforeToggle2ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        jFileChooser1.setApproveButtonText("OK");
        int ret = jFileChooser1.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION)
        {
            File fname = jFileChooser1.getSelectedFile();
            jLookIn.getModel().setSelectedItem(fname.getPath());
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup ContentSearchType;
    private javax.swing.ButtonGroup FilenameSearchType;
    private javax.swing.JButton jAfter;
    private javax.swing.JButton jAfter1;
    private javax.swing.JButton jAfter2;
    private javax.swing.JSpinner jAfterSpinner;
    private javax.swing.JSpinner jAfterSpinner1;
    private javax.swing.JSpinner jAfterSpinner2;
    private javax.swing.JToggleButton jAfterToggle;
    private javax.swing.JToggleButton jAfterToggle1;
    private javax.swing.JToggleButton jAfterToggle2;
    private javax.swing.JButton jBefore;
    private javax.swing.JButton jBefore1;
    private javax.swing.JButton jBefore2;
    private javax.swing.JSpinner jBeforeSpinner;
    private javax.swing.JSpinner jBeforeSpinner1;
    private javax.swing.JSpinner jBeforeSpinner2;
    private javax.swing.JToggleButton jBeforeToggle;
    private javax.swing.JToggleButton jBeforeToggle1;
    private javax.swing.JToggleButton jBeforeToggle2;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox<String> jContainingText;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JComboBox<String> jFileName;
    private javax.swing.JComboBox<String> jFileSizeScaler;
    private javax.swing.JSpinner jGreaterThanSpinner;
    private javax.swing.JToggleButton jGreaterThanToggle;
    private javax.swing.JCheckBox jIgnoreHiddenFiles;
    private javax.swing.JCheckBox jIgnoreHiddenFolders;
    private javax.swing.JCheckBox jIgnoreSymbolicLinks;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSpinner jLessThanSpinner;
    private javax.swing.JToggleButton jLessThanToggle;
    private javax.swing.JComboBox<String> jLookIn;
    private javax.swing.JPanel jLookInPanel;
    private javax.swing.JPanel jModifiedPanel;
    private javax.swing.JPanel jModifiedPanel1;
    private javax.swing.JPanel jModifiedPanel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JCheckBox jSubFolders;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton jUseContentRegex;
    private javax.swing.JRadioButton jUseContentSearch;
    private javax.swing.JRadioButton jUseFileGlobs;
    private javax.swing.JRadioButton jUseFileRegex;
    // End of variables declaration//GEN-END:variables
}
