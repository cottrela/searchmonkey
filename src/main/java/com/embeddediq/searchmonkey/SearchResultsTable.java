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
import javax.swing.table.AbstractTableModel;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.FontMetrics;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author cottr
 */
public class SearchResultsTable extends javax.swing.JPanel {

    private Timer timer;
    private List<SearchResult> rowData = new ArrayList<>();
    private JTable table;
    private MyTableModel myModel;
    
    /**
     * Creates new form SearchResults
     * @param queue
     * @param rateMillis
     */
    public SearchResultsTable() {
        initComponents();

        myModel = new MyTableModel();
        table = new JTable(myModel);
        table.setDefaultRenderer(Object.class, new SearchMonkeyTableRenderer());
        table.setAutoCreateRowSorter(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Show/hide columns using this code
        /*
        TableColumn hidden = table.getColumn(SearchResult.COLUMN_NAMES[SearchResult.ACCESSED]);
        table.removeColumn(hidden);
        table.addColumn(hidden);
        table.moveColumn(last_pos, new_pos);
        */
                
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
       
    }
    
    /*
    public void setQueue(SearchResultQueue queue, int rateMillis)
    {
        // Start thread timer
        timer = new Timer(rateMillis, new ResultsListener(queue, rowData));
    }
    public void getQueue(SearchResultQueue queue, int rateMillis)
    {
        // Start thread timer
        timer = new Timer(rateMillis, new ResultsListener(queue, rowData));
    }
    */
 
    public void resizeAllColumnWidth() {
    final TableColumnModel columnModel = table.getColumnModel();
    for (int column = 0; column < table.getColumnCount(); column++) {
        int width = 15; // Min width
        for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component comp = table.prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width +1 , width);
        }
        if(width > 300)
            width=300;
        columnModel.getColumn(column).setPreferredWidth(width);
    }
}

    // Call this at the start of the search
    public void start(SearchResultQueue queue, int rateMillis)
    {
        rowData.clear();
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        model.setRowCount(0);
        
//        if (table.getRowCount() > 0) {
//            table.removeRowSelectionInterval(0, table.getRowCount()-1); // Clear all
//        }
        timer = new Timer(rateMillis, new ResultsListener(queue, rowData));
        timer.start();
    }

    // Call this when the search is complete (or has been cancelled)
    public void stop()
    {
        timer.stop();
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
                // TODO - Not sure why -1 is required
                // TODO - Check that all rows are being shown
                myModel.fireTableRowsInserted(startRow, endRow - 1); // Not sure why - 1 is required... 
            }
        }        
    }
    
    class MyTableModel extends DefaultTableModel 
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
        /*
        @Override
        public boolean isCellEditable(int row, int col)
            { return false; }
        @Override
        public void setValueAt(Object value, int row, int col) {
            // TODO - allow user editing of these fields
            // rowData[row][col] = value;
            fireTableCellUpdated(row, col);
        }
        */
    }
    
    public class SearchMonkeyTableRenderer extends JLabel
                           implements TableCellRenderer {

        public SearchMonkeyTableRenderer() {
            setOpaque(true); //MUST do this for background to show up.
        }
        
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

            // int idx = table.convertColumnIndexToModel(colunn)
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
                        // this.setIcon(hidden_file_icon);
                        flagText.add("HIDDEN");
                    }
                    if (flags == SearchResult.SYMBOLIC_LINK){
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 945, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 368, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


}
