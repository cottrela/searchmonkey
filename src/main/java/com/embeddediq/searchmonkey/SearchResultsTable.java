/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author cottr
 */
public class SearchResultsTable extends javax.swing.JPanel implements ItemListener {

    private final List<SearchResult> rowData;
    private final MyTableModel myModel;
    private final Preferences prefs;
    
    /**
     * Creates new form SearchResults
     */
    public SearchResultsTable() {
        prefs = Preferences.userNodeForPackage(SearchEntry.class);
        initComponents();

        rowData = new ArrayList<>();
        myModel = new MyTableModel();
        jTable1.setModel(myModel);
        jTable1.setDefaultRenderer(Object.class, new SearchMonkeyTableRenderer());
        jTable1.setAutoCreateRowSorter(true);
        jTable1.setFillsViewportHeight(true);
        jTable1.getColumn(SearchResult.COLUMN_NAMES[SearchResult.FLAGS]).setCellRenderer(new IconTableRenderer(jTable1.getRowHeight()-6));
        
        // Check to see if the Desktop().edit function is supported
        this.jOpen.setVisible(Desktop.getDesktop().isSupported(Desktop.Action.OPEN));
        this.jEdit.setVisible(Desktop.getDesktop().isSupported(Desktop.Action.EDIT));
        this.jBrowse.setVisible(Desktop.getDesktop().isSupported(Desktop.Action.BROWSE));
        

        Restore();
    }
    
    private void restoreColumnOrder(String name, Object def)
    {
        Gson g = new Gson();
        
        String strdef = prefs.get(name, g.toJson(def));
        int[] indices = g.fromJson(strdef, int[].class);
        
        TableColumnModel columnModel = jTable1.getColumnModel();
        
        TableColumn column[] = new TableColumn[indices.length];

        for (int i = 0; i < column.length; i++) {
            column[i] = columnModel.getColumn(indices[i]);
        }

        while (columnModel.getColumnCount() > 0) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }

        for (int i = 0; i < column.length; i++) {
            columnModel.addColumn(column[i]);
        }
        
        Set<Integer> cols = new HashSet<>();
        for (int i: indices) {
            cols.add(i);
        }
        // Add an entry for each column
        jColumnMenu.removeAll();
        for (int i=0; i<SearchResult.COLUMN_NAMES.length; i++)
        {
            String column2 = SearchResult.COLUMN_NAMES[i];
            JCheckBoxMenuItem item = new JCheckBoxMenuItem();
            item.setName(column2);
            item.setText(column2);
            item.setSelected(cols.contains(i));
            item.addItemListener(this);
            jColumnMenu.add(item);
        }
    }
    private void restoreColumnWidth(String name, Object def)
    {
        Gson g = new Gson();
        
        String strdef = prefs.get(name, g.toJson(def));
        int[] indices = g.fromJson(strdef, int[].class);
        
        for (int i=0; i<SearchResult.COLUMN_NAMES.length; i++)
        {
            jTable1.getColumn(SearchResult.COLUMN_NAMES[i]).setPreferredWidth(indices[i]);
        }
    }

    private void storeColumnWidth(String name)
    {
        Gson g = new Gson();
        int[] indices = new int[SearchResult.COLUMN_NAMES.length];
        for (int i=0; i<SearchResult.COLUMN_NAMES.length; i++)
        {
            indices[i] = jTable1.getColumn(SearchResult.COLUMN_NAMES[i]).getWidth();
        }
        prefs.put(name, g.toJson(indices));
    }
    private void storeColumnOrder(String name)
    {
        // Read back the current column ordering
        TableColumnModel columnModel = jTable1.getColumnModel();
        int column[] = new int[columnModel.getColumnCount()];
        for (int i = 0; i < column.length; i++) {
            column[i] = columnModel.getColumn(i).getModelIndex();
        }
        
        // Store the current column order
        Gson g = new Gson();
        prefs.put(name, g.toJson(column));
    }

    public void Save()
    {
        storeColumnWidth("ColumnWidth");
        storeColumnOrder("ColumnOrder");
    }
    
    public void Restore()
    {
        restoreColumnWidth("ColumnWidth", SearchResult.COLUMN_WIDTH);
        restoreColumnOrder("ColumnOrder", IntStream.range(0, SearchResult.COLUMN_WIDTH.length).toArray());
        jTable1.doLayout();
    }
        
    // Use with SearchWorker
    public void clearTable()
    {
        // rowData.clear();
        myModel.clear(); // setRowCount(0);
        // myModel.fireTableDataChanged();
    }
    public void insertRows(List<SearchResult> results)
    {
        //int s = myModel.getRowCount();
        //int e = s + results.size() - 1;
        //rowData.addAll(results);
        myModel.addRows(results);
        // myModel.addRow((Object[])results);
        //myModel.fireTableRowsInserted(s, e);
    }
    // Use with SearchWorker
    
    public void addListSelectionListener(ListSelectionListener listener)
    {
        jTable1.getSelectionModel().addListSelectionListener(listener);
    }
   

    public SearchResult[] getSelectedRows()
    {
        int[] rows = jTable1.getSelectedRows();
        SearchResult[] results = new SearchResult[rows.length];
        for (int i=0; i<rows.length; i++)
        {
            results[i] = rowData.get(jTable1.convertRowIndexToModel(rows[i]));
        }        
        return results;
    }

    public void resizeAllColumnWidth() {
    final TableColumnModel columnModel = jTable1.getColumnModel();
    for (int column = 0; column < jTable1.getColumnCount(); column++) {
        int width = 15; // Min width
        for (int row = 0; row < jTable1.getRowCount(); row++) {
            TableCellRenderer renderer = jTable1.getCellRenderer(row, column);
            Component comp = jTable1.prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width +1 , width);
        }
        if(width > 300) {
            width=300;
        }
        columnModel.getColumn(column).setPreferredWidth(width);
    }
}

    private class MyTableModel extends DefaultTableModel 
    {
        @Override
        public String getColumnName(int col) {
            return SearchResult.COLUMN_NAMES[col];
        }
        
        @Override
        public Class<?> getColumnClass(int col)
        {
            return SearchResult.COLUMN_CLASSES[col];
        }
        
        public void addRows(List<SearchResult> objects) { 
            for (SearchResult vals: objects) {
                rowData.add(vals);
//                super.addRow(vals.toArray());
            }
            fireTableDataChanged();
        }
        @Override
        public int getRowCount() { 
            return rowData.size();
        }

        public void clear() {
            rowData.clear();
            fireTableDataChanged();
        }
        @Override
        public int getColumnCount() {
            return SearchResult.COLUMN_NAMES.length; // Constan
        }
        @Override
        public Object getValueAt(int row, int col) {
            SearchResult val = rowData.get(jTable1.convertRowIndexToModel(row));
            return val.get(col);
        }
        @Override
        public boolean isCellEditable(int row, int col)
            { return false; }

//        @Override
//        public void setValueAt(Object value, int row, int col) {
//            // TODO - allow user editing of these fields
//            SearchResult val = rowData.get(row);
//            switch (col) {
//                case SearchResult.FILENAME:
//                    val.fileName = (String)value;
//                    break;
//                case SearchResult.FOLDER:
//                    val.pathName = (String)value;
//                    break;
//                case SearchResult.COUNT:
//                    val.matchCount = (int)value;
//                    break;
//                case SearchResult.SIZE:
//                    val.fileSize = (long)value;
//                    break;
//                case SearchResult.CREATED:
//                    val.creationTime = (FileTime)value;
//                    break;
//                case SearchResult.MODIFIED:
//                    val.lastModified = (FileTime)value;
//                    break;
//                case SearchResult.ACCESSED:
//                    val.lastAccessTime = (FileTime)value;
//                    break;
//                case SearchResult.FLAGS:
//                    val.fileFlags = (int)value;
//                    break;
//                case SearchResult.EXTENSION:
//                    val.fileExtension = (String)value;
//                    break;
//                case SearchResult.CONTENT_TYPE:
//                    val.contentType = (String)value;
//                    break;
//                default:
//                    break;
//            }
//            // rowData[row] // [col] = value;
//            fireTableCellUpdated(row, col);
//        }
    }
    
    class IconTableRenderer extends DefaultTableCellRenderer // JPanel implements UIResource
    {

        private final JLabel hidden;
        private final JLabel linked;
        public IconTableRenderer(int height) {
            setOpaque(true); //MUST do this for background to show up.
            hidden = new JLabel(getScaledIcon(getClass().getResource("/images/File-Hide-icon.png"), height));
            hidden.setToolTipText("Hidden file");
            linked = new JLabel(getScaledIcon(getClass().getResource("/images/link-icon-614x460.png"), height));
            linked.setToolTipText("Symbolic link");
            this.setLayout(new FlowLayout());
        }

        private Icon getScaledIcon(URL srcImg, int height) {
            ImageIcon image = new ImageIcon(srcImg);
            
            image.setImage(getScaledImage(image.getImage(), height));
            return (Icon)image;
        }

        private Image getScaledImage(Image srcImg, int height){
            int w = height * srcImg.getWidth(this) / srcImg.getHeight(this);
            BufferedImage resizedImg = new BufferedImage(w, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resizedImg.createGraphics();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(srcImg, 0, 0, w, height, null);
            g2.dispose();

            return resizedImg;
        }

        /**
         *
         * @param table
         * @param value
         * @param isSelected
         * @param hasFocus
         * @param row
         * @param column
         * @return
         */
        @Override
        public Component getTableCellRendererComponent(
                                JTable table, Object value,
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {
            JPanel panel = new JPanel();
            int flags = (int)value;
            List<String> flagText = new ArrayList<>();
            if (flags == SearchResult.HIDDEN_FILE)
            {
                panel.add(hidden);
                //setIcon(hidden);
                flagText.add("Hidden file");
            }
            if (flags == SearchResult.SYMBOLIC_LINK){
                panel.add(linked);
                // setIcon(linked);
                flagText.add("Symbolic link");
            }
            // Update tool tips
            String txtToolTip = String.join(", ", flagText); 
            if (txtToolTip.isEmpty())
            {
                txtToolTip = "Normal file";
            }
            panel.setToolTipText(txtToolTip);
            if (isSelected)
            {
                panel.setForeground(table.getSelectionForeground());                
                panel.setBackground(table.getSelectionBackground());                
            } else {
                panel.setForeground(table.getForeground());                
                panel.setBackground(table.getBackground());                
            }
            return panel;
            
        }
    }
    
    public class SearchMonkeyTableRenderer extends JLabel
                           implements TableCellRenderer {

        public SearchMonkeyTableRenderer() {
            setOpaque(true); //MUST do this for background to show up.
            hidden = new ImageIcon(getClass().getResource("/images/File-Hide-icon.png"));
            linked = new ImageIcon(getClass().getResource("/images/link-icon-614x460.png"));
        }
        Icon hidden;
        Icon linked;
        
        private final String[] MAG_NAMES = new String[] {"Bytes", "KBytes", "MBytes", "GBytes", "TBytes"};
    
        private int getFileOrder(long fsize)
        {
            int order = 0;
            while (fsize > 1024)
            {
                order ++;
                fsize /= 1024;
            }
            if (order > 1) order --;
            return order;
        }
        
        /**
         *
         * @param table
         * @param value
         * @param isSelected
         * @param hasFocus
         * @param row
         * @param column
         * @return
         */
        @Override
        public Component getTableCellRendererComponent(
                                JTable table, Object value,
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {

            setIcon(null);

            String txtVal = "";
            String txtToolTip = new String();
            int idx = table.convertColumnIndexToModel(column);
            switch (idx) {
                case SearchResult.SIZE: // Handle Size
                    int order = getFileOrder((long)value);
                    if (order > 0) {
                        txtVal = String.format("%.1f %s", ((double)((long)value) / Math.pow(1024, order)), MAG_NAMES[order]);
                    } else {
                        txtVal = String.format("%d %s", (long)value, MAG_NAMES[order]);
                    }
                    break;
                case SearchResult.CREATED: // Handle Date
                case SearchResult.ACCESSED:
                case SearchResult.MODIFIED:
                    FileTime ft = (FileTime)value;
                    LocalDateTime ldt = LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault());
                    txtVal = ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT));
                    txtToolTip = ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM));
                    break;
                case SearchResult.FLAGS: // Handle Flags
                    int flags = (int)value;
                    List<String> flagText = new ArrayList<>();
                    if (flags == SearchResult.HIDDEN_FILE)
                    {
                        //this.add(hidden);
                        //setIcon(hidden);
                        flagText.add("HIDDEN");
                    }
                    if (flags == SearchResult.SYMBOLIC_LINK){
                        //this.add(linked);
                        // setIcon(linked);
                        flagText.add("SYMBOLIC");
                    }
                    txtVal = String.join(", ", flagText); 
                    if (txtVal.isEmpty())
                    {
                        txtToolTip = "Normal file";
                    }
                    break;
                case SearchResult.COUNT: // Handle Count
                    int count = (int)value;
                    if (count < 0)
                    {
                        txtVal = "N/A"; // Not applicable
                        txtToolTip = "Not applicable";
                       break;
                    } else {
                        txtToolTip = String.format("%d Match%s", count, (count > 1 ? "es" : ""));
                    }
                default:
                    if (value != null) {
                        txtVal = value.toString();
                    }
                    break;
            }

            // Create text, and tooltips to match
            setText(txtVal);
            if (txtToolTip.isEmpty()) txtToolTip = txtVal;
            if (!txtToolTip.isEmpty()) setToolTipText(txtToolTip);
            
            // Allow selection
            if (isSelected)
            {
                // selectedBorder is a solid border in the color
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                // unselectedBorder is a solid border in the color
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            return this;
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

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jOpen = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jEdit = new javax.swing.JMenuItem();
        jBrowse = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jColumnMenu = new javax.swing.JMenu();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jPopupMenu1.setLabel("Hello");
        jPopupMenu1.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                jPopupMenu1PopupMenuWillBecomeVisible(evt);
            }
        });

        jOpen.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jOpen.setMnemonic('O');
        jOpen.setText("Open file");
        jOpen.setToolTipText("Open selected file or files using the applicaiton associated with this file type");
        jOpen.setEnabled(false);
        jOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jOpenActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jOpen);
        jPopupMenu1.add(jSeparator1);

        jEdit.setMnemonic('E');
        jEdit.setText("Edit file");
        jEdit.setToolTipText("Edit select file or files using the default editor");
        jEdit.setEnabled(false);
        jEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jEdit);

        jBrowse.setMnemonic('B');
        jBrowse.setText("Browse folder");
        jBrowse.setToolTipText("Open file navigator to browse the folder containing this file");
        jBrowse.setEnabled(false);
        jBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBrowseActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jBrowse);
        jPopupMenu1.add(jSeparator2);

        jColumnMenu.setMnemonic('C');
        jColumnMenu.setText("Show/Hide Columns");
        jColumnMenu.setToolTipText("Show or hide columns in this table");
        jPopupMenu1.add(jColumnMenu);

        setLayout(new java.awt.BorderLayout());

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable1.setComponentPopupMenu(jPopupMenu1);
        jTable1.setFillsViewportHeight(true);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                jTable1MousePressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void itemStateChanged(ItemEvent ie) {
        JCheckBoxMenuItem item = (JCheckBoxMenuItem)ie.getItem();
        String menuName = item.getName();
        if (ie.getStateChange() == ItemEvent.SELECTED)
        {
            TableColumnModel tcm = jTable1.getColumnModel();
            int idx;
            for (idx=0; idx<SearchResult.COLUMN_NAMES.length; idx++) {
                if (SearchResult.COLUMN_NAMES[idx].equals(menuName)) break;
            }
            //int idx2 = tcm.getColumn(idx);
            TableColumn col = new TableColumn(idx, SearchResult.COLUMN_WIDTH[idx]);
            //TableColumn col = tcm.getColumn(idx);
            jTable1.addColumn(col);
            jTable1.moveColumn(jTable1.getColumnCount() - 1, idx);
        } else {
            TableColumn col = jTable1.getColumn(menuName);
            jTable1.removeColumn(col);
            
        }
    }

    boolean colsVisible[] = new boolean[9];
    private void jPopupMenu1PopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_jPopupMenu1PopupMenuWillBecomeVisible
        boolean enable = true;
        int count = jTable1.getSelectedRowCount();
        if (count > 0)
        {
            jOpen.setEnabled(enable);
            jEdit.setEnabled(enable);
            if (count > 1) {
                jOpen.setText(String.format("Open %d files", count));
                jEdit.setText(String.format("Edit %d files", count));
            }
        }
        
        count = getUniqueFolders().length;
        if (count > 0)
        {
            jBrowse.setEnabled(enable);
            if (count > 1) {
                jBrowse.setText(String.format("Browse %d folders", count));
            }
        }
    }//GEN-LAST:event_jPopupMenu1PopupMenuWillBecomeVisible

    String[] getUniqueFolders()
    {
        Set<String> folders = new HashSet<>();
        int[] rows = this.jTable1.getSelectedRows();
        for (int row: rows)
        {
            SearchResult result = rowData.get(jTable1.convertRowIndexToModel(row));
            folders.add(result.pathName);
        }
        return (String[]) folders.toArray(new String[folders.size()]);
    }
    
    private void jOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jOpenActionPerformed
        int[] rows = this.jTable1.getSelectedRows();
        for (int row: rows) {
            Open(row);
        }
    }//GEN-LAST:event_jOpenActionPerformed

    private void Open(int row)
    {
        SearchResult result = rowData.get(jTable1.convertRowIndexToModel(row));
        File file = new File(result.pathName, result.fileName);
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            Logger.getLogger(SearchResultsTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void Edit(int row)
    {
        SearchResult result = rowData.get(jTable1.convertRowIndexToModel(row));
        File file = new File(result.pathName, result.fileName);
        try {
            Desktop.getDesktop().edit(file);
        } catch (IOException ex) {
            Logger.getLogger(SearchResultsTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void Browse(String folder)
    {
        File file = new File(folder);
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            Logger.getLogger(SearchResultsTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void jTable1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MousePressed
        if (evt.getClickCount() > 1) {
            // Double click handler]
            Point point = evt.getPoint();
            int row = jTable1.rowAtPoint(point);
            if (row != -1)
            {
                Open(row);
            }
        }
    }//GEN-LAST:event_jTable1MousePressed

    private void jBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBrowseActionPerformed
        String[] folders = getUniqueFolders();
        // int[] rows = this.jTable1.getSelectedRows();
        for (String folder: folders) {
            Browse(folder);
        }
    }//GEN-LAST:event_jBrowseActionPerformed

    private void jEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEditActionPerformed
        int[] rows = this.jTable1.getSelectedRows();
        for (int row: rows) {
            Edit(row);
        }
    }//GEN-LAST:event_jEditActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jBrowse;
    private javax.swing.JMenu jColumnMenu;
    private javax.swing.JMenuItem jEdit;
    private javax.swing.JMenuItem jOpen;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables


}
