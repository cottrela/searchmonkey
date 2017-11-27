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
import static com.embeddediq.searchmonkey.SearchResult.COLUMN_NAMES;
import java.awt.Color;
import java.awt.Component;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author cottr
 */
public class SearchResults extends javax.swing.JPanel {

    private Timer timer;
    private List<SearchResult> rowData = new ArrayList<>();
    private JTable table;
    private MyTableModel myModel;
    
    /**
     * Creates new form SearchResults
     * @param queue
     * @param rateMillis
     */
    public SearchResults(SearchResultQueue queue, int rateMillis) {
        initComponents();

/*        
        ColumnOrder = new ArrayList<>();
        for (int i=0; i< COLUMN_NAMES.length; i++)
        {
            ColumnOrder.add(i);
        }
        //table.getColumn(1).s
*/
        
        myModel = new MyTableModel();
        table = new JTable(myModel);
        table.setDefaultRenderer(Object.class, new SearchMonkeyTableRenderer());
        table.setAutoCreateRowSorter(true);
                
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
       
        // Start thread timer
        timer = new Timer(rateMillis, new ResultsListener(queue, rowData));
    }
    

    // Call this at the start of the search
    public void start()
    {
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

    /*
    private List<Integer> ColumnOrder; //  = new ArrayList<>();
    public void insertColumn(int columnIdent, int position)
    {
        ColumnOrder.add(columnIdent, position);
    }
    public void removeColumn(int position)
    {
        ColumnOrder.remove(position);
    }
    */
    
    
    class MyTableModel extends AbstractTableModel 
    {
        @Override
        public String getColumnName(int col) {
            return SearchResult.COLUMN_NAMES[col];
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

        public Component getTableCellRendererComponent(
                                JTable table, Object color,
                                boolean isSelected, boolean hasFocus,
                                int row, int column) {

            // int idx = table.convertColumnIndexToModel(colunn)
            int idx = table.convertColumnIndexToModel(column);
            switch (idx) {
                case SearchResult.SIZE: // Handle Size
                    int mag = 0; // Bytes
                    double val = (double)((long)color);
                    while (val > 1024)
                    {
                        mag ++; // KB, MB, GB, TB, etc
                        val /= 1024;
                    }
                    setText(String.format("%.1f %s", val, MAG_NAMES[mag]));
                    break;
                case SearchResult.CREATED: // Handle Date
                case SearchResult.ACCESSED:
                case SearchResult.MODIFIED:
                    FileTime ft = (FileTime)color;
                    LocalDateTime ldt = LocalDateTime.ofInstant(ft.toInstant(), ZoneId.systemDefault());
                    
                    setText(ldt.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)));
                    
                    break;
                case SearchResult.FLAGS: // Handle Flags
                    int flags = (int)color;
                    List<String> flagText = new ArrayList<>();
                    if (flags == SearchResult.HIDDEN_FILE)
                    {
                        // this.setIcon(hidden_file_icon);
                        flagText.add("HIDDEN");
                    }
                    if (flags == SearchResult.SYMBOLIC_LINK){
                        flagText.add("SYMBOLIC");
                    }
                    this.setText(String.join(", ", flagText));
                    break;
                case SearchResult.COUNT: // Handle Count
                    int count = (int)color;
                    if (count < 0)
                    {
                        setText("N/A"); // Not applicable
                       break;
                    } // Otherwise use default renderer
                default:
                    this.setText(color.toString());
                    break;
            }
            
            // Allow selection
            if (isSelected) {
                // selectedBorder is a solid border in the color
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                // unselectedBorder is a solid border in the color
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }

            setToolTipText("hello world!!"); //Discussed in the following section
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
