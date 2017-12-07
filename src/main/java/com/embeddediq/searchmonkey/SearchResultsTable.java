/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.scene.layout.Border;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author cottr
 */
public class SearchResultsTable extends javax.swing.JPanel implements ListSelectionListener {

    private Timer timer;
    private final List<SearchResult> rowData;
    // private final JTable table;
    private final MyTableModel myModel;
    
    /**
     * Creates new form SearchResults
     */
    public SearchResultsTable() {
        initComponents();

        rowData = new ArrayList<>();
        myModel = new MyTableModel();
        jTable1.setModel(myModel);
        jTable1.setDefaultRenderer(Object.class, new SearchMonkeyTableRenderer());
        jTable1.setAutoCreateRowSorter(true);
        //jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable1.setFillsViewportHeight(true);
        // table = jTable1;
        jTable1.getColumn(SearchResult.COLUMN_NAMES[SearchResult.FLAGS]).setCellRenderer(new IconTableRenderer(jTable1.getRowHeight()-6));

        jTable1.getSelectionModel().addListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting()) {
            return;
        }

        parent.ClearContent();
        int[] rows = jTable1.getSelectedRows();
        for (int row: rows)
        {
            SearchResult val = rowData.get(jTable1.convertRowIndexToModel(row));
            parent.UpdateContent(val);
        }        
    }

    private Searchmonkey parent;
    public void setParent(Searchmonkey parent)
    {
        this.parent = parent;
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

    // Call this at the start of the search
    public void start(SearchResultQueue queue, int rateMillis)
    {
        // Clear the previous entries
        rowData.clear();
        DefaultTableModel model = (DefaultTableModel)jTable1.getModel();
        model.setRowCount(0);
        
        // Start a new session
        this.queue = queue;
        timer = new Timer(rateMillis, new ResultsListener(queue, rowData));
        timer.start();
    }
    
    private SearchResultQueue queue;

    // Call this when the search is complete (or has been cancelled)
    public void stop()
    {
        timer.stop();
        actionCompleted();
    }

    public void actionCompleted()
    {
        int sz = queue.size();
        if (sz > 0)
        {
            int startRow = myModel.getRowCount();
            int endRow = startRow + sz;
            queue.drainTo(rowData);
            myModel.fireTableRowsInserted(startRow, endRow - 1);
        }
    }
    
    private class ResultsListener implements ActionListener
    {
        private final List<SearchResult> results;
        private final SearchResultQueue queue;
        public ResultsListener(SearchResultQueue queue, List<SearchResult> results)
        {
            this.queue = queue;
            this.results = results;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int sz = queue.size();
            if (sz > 0)
            {
                int startRow = myModel.getRowCount();
                int endRow = startRow + sz;
                queue.drainTo(results);
                myModel.fireTableRowsInserted(startRow, endRow - 1);
            }
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
        
        @Override
        public int getRowCount() { 
            return rowData.size();
        }
        @Override
        public int getColumnCount() {
            return SearchResult.COLUMN_NAMES.length; // Constan
        }
        @Override
        public Object getValueAt(int row, int col) {
            SearchResult val = rowData.get(row);
            return val.get(col);
        }
        @Override
        public boolean isCellEditable(int row, int col)
            { return false; }
        /*
        @Override
        public void setValueAt(Object value, int row, int col) {
            // TODO - allow user editing of these fields
            // rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }
        */
    }
    
    class IconTableRenderer extends JPanel implements TableCellRenderer
    {

        private final JLabel hidden;
        private final JLabel linked;
        public IconTableRenderer(int height) {
            setOpaque(true); //MUST do this for background to show up.
            hidden = new JLabel(getScaledIcon(getClass().getResource("/images/File-Hide-icon.png"), height));
            hidden.setToolTipText("Hidden file");
            linked = new JLabel(getScaledIcon(getClass().getResource("/images/link-icon-614x460.png"), height));
            linked.setToolTipText("Symbolic link");
            //setBorder(Border.EMPTY);
            //hidden.setBorder(Border.EMPTY);
            //linked.setBorder(Border.EMPTY);
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
            int flags = (int)value;
            this.removeAll();
            List<String> flagText = new ArrayList<>();
            if (flags == SearchResult.HIDDEN_FILE)
            {
                this.add(hidden);
                //setIcon(hidden);
                flagText.add("Hidden file");
            }
            if (flags == SearchResult.SYMBOLIC_LINK){
                this.add(linked);
                // setIcon(linked);
                flagText.add("Symbolic link");
            }
            // Update tool tips
            String txtToolTip = String.join(", ", flagText); 
            if (txtToolTip.isEmpty())
            {
                txtToolTip = "Normal file";
            }
            this.setToolTipText(txtToolTip);
//            break;
            return this;
            
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

            String txtVal;
            String txtToolTip = new String();
            int idx = table.convertColumnIndexToModel(column);
            switch (idx) {
                case SearchResult.SIZE: // Handle Size
                    int mag = 0; // Bytes
                    double val = (double)((long)value);
                    while (val > 1024)
                    {
                        mag ++; // KB, MB, GB, TB, etc
                        val /= 1024;
                    }
                    txtVal = String.format("%.1f %s", val, MAG_NAMES[mag]);
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
                    txtVal = value.toString();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

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
        jTable1.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(jTable1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables


}
