/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

/**
 *
 * @author adam
 */
public class CalendarPopup extends javax.swing.JPanel {

    Calendar calendar;
    /**
     * Creates new form CalendarPopup
     */
    public CalendarPopup() {
        initComponents();

        calendar = GregorianCalendar.getInstance();
        baseCal = GregorianCalendar.getInstance();
        UpdateCalendar();
        
        // This is being overriden by the HMI
        jTable1.setCellSelectionEnabled(true);

        // Handle row changes
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener () {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;
                updateDate();
            }
        });

        // Handle column changes
        jTable1.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener () {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                updateDate();
            }
        });
    
        
    }
    
    Date date;
    public void setDate(Date date)
    {
        // this.date = date;
        calendar.setTime(date);
        UpdateCalendar();
        //int count = calendar - baseVal;
        //jTable1.getColumnModel().getSelectionModel().addSelectionInterval(, WIDTH);
        //TODO - select the cell after it has been chosen
    }
    public Date getDate()
    {
        return this.date;
    }
    private Collection<ChangeListener> listeners = new LinkedList<>();
    public void addChangeListener(ChangeListener listener)
    {
        listeners.add(listener);
    }
    
    private void updateDate()
    {
        Calendar my_copy = (Calendar) baseCal.clone();
        int col = jTable1.getSelectedColumn();
        int row = jTable1.getSelectedRow();
        my_copy.add(Calendar.DAY_OF_MONTH, col + (row * 7));
        this.date = my_copy.getTime();
        
        // Implement a change listener
        for(ChangeListener listener: listeners){
            listener.stateChanged(new ChangeEvent(this));
        }        
    }
    
    private void UpdateCalendar()
    {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        int fd = calendar.getFirstDayOfWeek();
        
        // Update days of the week titles
        Map<String, Integer>weeknames = calendar.getDisplayNames(Calendar.DAY_OF_WEEK, Calendar.SHORT_STANDALONE, getLocale());
        weeknames.entrySet().forEach((entry) -> {
            int col = entry.getValue() - fd;
            if (col < 0) {
                col += 7;
            } //
            String dow = entry.getKey();
            TableColumn column = jTable1.getTableHeader().getColumnModel().getColumn(col);
            column.setHeaderValue(dow.substring(0,1));
            // TODO - highlight today's date
        });
        
        // Update month name
        String m = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG_STANDALONE, getLocale());
        int y = calendar.get(Calendar.YEAR);
        this.jLabel1.setText(String.format("%s %d", m, y));

        // Clear first and last rows
        for (int i = 0; i<7 ; i++)
        {
            jTable1.getModel().setValueAt(null, 0, i);
            jTable1.getModel().setValueAt(null, jTable1.getRowCount()-1, i);
        }

        // Update days of the month
        {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int wd = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            int last_day = calendar.get(Calendar.DAY_OF_MONTH); // Get the last day
            int col = wd - fd;
            if (col < 0) {
                col += 7;
            }
            int row = 0;
            for (int i = 0; i<last_day; i++)
            {
                jTable1.getModel().setValueAt(i+1, row, col);
                col ++;
                if (col >= 7)
                {
                    col = 0;
                    row ++;
                }
            }    
        }

        // Set all of the days from the calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        baseCal.setTime(calendar.getTime());
        // baseCal = (Calendar)calendar.clone(); // set(Calendar.DAY_OF_MONTH, 1);
        int cM = baseCal.get(Calendar.MONTH);
        int wd2 = baseCal.get(Calendar.DAY_OF_WEEK);
        //calendar.add(Calendar.MONTH, 1);
        //calendar.add(Calendar.DAY_OF_MONTH, -1);
        // int last_day = calendar.get(Calendar.DAY_OF_MONTH); // Get the last day
        int col2 = wd2 - fd;
        if (col2 < 0) {
            col2 += 7;
        }
        //calendar.set(Calendar.DAY_OF_MONTH, 1);
        baseCal.add(Calendar.DAY_OF_MONTH, -col2); // Go back N days
        Calendar tmp = (Calendar)baseCal.clone();
        for (int row=0; row<jTable1.getRowCount(); row++)
        {
            for (int col=0; col<7; col++)
            {
                // TODO - change the cell values if from last month
                int val = tmp.get(Calendar.DAY_OF_MONTH);
                jTable1.getModel().setValueAt(val, row, col);
                tmp.add(Calendar.DAY_OF_MONTH, 1); // next
            }
        }
        updateDate();
    }

    private Calendar baseCal; //  = Calendar.getInstance();
    

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
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(180, 150));
        setMinimumSize(new java.awt.Dimension(180, 150));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(180, 150));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "M", "T", "W", "T", "F", "S", "S"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setAutoscrolls(false);
        jTable1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTable1.setFillsViewportHeight(true);
        jTable1.setIntercellSpacing(new java.awt.Dimension(0, 0));
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(0).setPreferredWidth(20);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setPreferredWidth(20);
            jTable1.getColumnModel().getColumn(2).setResizable(false);
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(20);
            jTable1.getColumnModel().getColumn(3).setResizable(false);
            jTable1.getColumnModel().getColumn(3).setPreferredWidth(20);
            jTable1.getColumnModel().getColumn(4).setResizable(false);
            jTable1.getColumnModel().getColumn(4).setPreferredWidth(20);
            jTable1.getColumnModel().getColumn(5).setResizable(false);
            jTable1.getColumnModel().getColumn(5).setPreferredWidth(20);
            jTable1.getColumnModel().getColumn(6).setResizable(false);
            jTable1.getColumnModel().getColumn(6).setPreferredWidth(20);
        }

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("December 2017");
        jPanel1.add(jLabel1, java.awt.BorderLayout.CENTER);

        jButton1.setBackground(new java.awt.Color(102, 102, 102));
        jButton1.setText(">");
        jButton1.setBorder(null);
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton1.setDefaultCapable(false);
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        jButton2.setBackground(new java.awt.Color(102, 102, 102));
        jButton2.setText(">>");
        jButton2.setBorder(null);
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton2.setDefaultCapable(false);
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jPanel1.add(jPanel2, java.awt.BorderLayout.EAST);

        jButton4.setBackground(new java.awt.Color(102, 102, 102));
        jButton4.setText("<<");
        jButton4.setBorder(null);
        jButton4.setBorderPainted(false);
        jButton4.setContentAreaFilled(false);
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton4.setDefaultCapable(false);
        jButton4.setFocusPainted(false);
        jButton4.setFocusable(false);
        jButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton4);

        jButton3.setBackground(new java.awt.Color(102, 102, 102));
        jButton3.setText("<");
        jButton3.setBorder(null);
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jButton3.setDefaultCapable(false);
        jButton3.setFocusPainted(false);
        jButton3.setFocusable(false);
        jButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton3);

        jPanel1.add(jPanel3, java.awt.BorderLayout.WEST);

        add(jPanel1, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        calendar.add(Calendar.MONTH, -1);
        UpdateCalendar();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        calendar.add(Calendar.YEAR, 1);
        UpdateCalendar();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        calendar.add(Calendar.MONTH, 1);
        UpdateCalendar();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        calendar.add(Calendar.YEAR, -1);
        UpdateCalendar();
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
