/*
 * Copyright (C) 2018 cottr
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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author cottr
 */
public class RegexBuilder extends javax.swing.JPanel {

    /**
     * Creates new form RegexBuilder
     */
    public RegexBuilder() {
        initComponents();
        
        // Create data
        List<RegexExpression> data = new ArrayList<>();
        data.add(new RegexExpression(RegexExpression.EXP_DONT_KNOW, RegexExpression.POS_STARTS_WITH));
        data.add(new RegexExpression(RegexExpression.EXP_DONT_KNOW, 1));
        data.add(new RegexExpression(RegexExpression.EXP_DONT_KNOW, RegexExpression.POS_NEW_ROW));
        data.add(new RegexExpression(RegexExpression.EXP_DONT_KNOW, RegexExpression.POS_ENDS_WITH));
        
        RegexTableModel model = new RegexTableModel(data);
        this.jTable1.setModel(model);
        RegexCellEditor cellEdit = new RegexCellEditor();
        this.jTable1.setDefaultRenderer(RegexExpression.class, cellEdit);
        this.jTable1.setDefaultEditor(RegexExpression.class, cellEdit);
        this.jTable1.setRowHeight(60);
        
        cellEdit.addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent ce) {
                RegexCellEditor cellEdit2 = (RegexCellEditor)ce.getSource();
                RegexExpression exp = (RegexExpression)cellEdit2.getCellEditorValue();
                // cellEdit2.get
            }

            @Override
            public void editingCanceled(ChangeEvent ce) {
                // do nothing throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        }); 
    }

    public class RegexCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
       RegexCellComponent regex;

        public RegexCellEditor() {
            regex = new RegexCellComponent();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
            RegexExpression feed = (RegexExpression)value;
            RegexCellComponent tmp = new RegexCellComponent();
            tmp.updateData(feed, true, table);
            //regex.updateData(feed, true, table);
            return tmp;
        }
 
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            RegexExpression feed = (RegexExpression)value;
            regex.updateData(feed, isSelected, table);
            return regex;
        }
        
        @Override
        public Object getCellEditorValue() {
            // This is value returned
            return regex.getData();
        }
    }
    
    public class RegexTableModel extends AbstractTableModel {
        List regex;

        public RegexTableModel(List regex) {
            this.regex = regex;
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) return String.class;
            else if (columnIndex == 1) return RegexExpression.class;
            else if (columnIndex == 2) return String.class;
            return Object.class;
        }
        
        @Override
        public int getColumnCount() {
            return 3; // Idx, Expression, Action
        }
        
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) return "#";
            else if (columnIndex == 1) return "Expression";
            else if (columnIndex == 2) return "Action";
            return "Unknown";
        }
        
        @Override
        public int getRowCount() {
            return (regex == null) ? 0 : regex.size();
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ((RegexExpression)regex.get(rowIndex)).getPosition();
                case 1:
                    return regex.get(rowIndex);
                case 2:
                    return "Action";
                default:
                    break;
            }
            return "Unknown";
        }
        
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex)
        {
            RegexExpression r2 = (RegexExpression)(regex.get(rowIndex));
            switch (columnIndex) {
                case 0:
                    r2.position = (int)value;
                case 1:
                    RegexExpression regexIn = (RegexExpression)value;
                    r2.repetition = regexIn.repetition;
                    r2.expression = regexIn.expression;
                    r2.content = regexIn.content;
                    r2.flags = regexIn.flags;
                case 2:
                    // regex.position = value;
                    // return "Action";
                    break;
                default:
                    break;
            }
            // Precaution
            this.fireTableDataChanged();
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 1);
        }
    }
    
//    public class RegexExpression 
//    {
//        public RegexExpression(
//        {
//            this.expression = 0;
//            this.position = 0;
//            this.flags = 0;                    
//        }
//
//        public RegexExpression(int expression, int position)
//        {
//            this.expression = expression;
//            this.position = position;
//            this.flags = 0;                    
//        }
//        
//        public RegexExpression(RegexExpression copy)
//        {
//            this.expression = copy.expression;
//            this.position = copy.position;
//            this.flags = copy.flags;
//            this.repetition = copy.repetition;
//            this.content = copy.content;
//        }
//
//        // Enumeration for the expression
//        public static final int EXP_DONT_KNOW = 0;
//        public static final int EXP_EXACT_PHRASE = 1;
//        public static final int EXP_ONE_OF_THESE = 2;
//        public static final int EXP_WHITE_SPACE = 3;
//        public static final int EXP_NUMERIC = 4;
//        public static final int EXP_ALPHA = 5;
//        public static final int EXP_ANY_EXCEPT = 6;
//        public static final int EXP_ANY_CHAR = 7;
//        public int expression;
//
//        // Expression content
//        public Object content; // Typically this is a string, but it could be a list too
//        public String getExpression()
//        {
//            switch (expression) {
//                case EXP_DONT_KNOW:
//                    return "Don't know";
//                case EXP_EXACT_PHRASE:
//                    return (String)content;
//                case EXP_ONE_OF_THESE:
//                    if (content.getClass().isInstance(List.class))
//                        return String.join(" OR ", (List)content);
//                    return content.toString();
//                case EXP_WHITE_SPACE:
//                    return "White space";
//                case EXP_NUMERIC:
//                    return "Any number: 0-9";
//                case EXP_ALPHA:
//                    return "Any letter: a-z or A-Z";
//                case EXP_ANY_CHAR:
//                    if (content.getClass().isInstance(List.class))
//                        return "Any of these characters: " + String.join("", (List)content);
//                    return "Any of these chars: " + content.toString();
//                case EXP_ANY_EXCEPT:
//                    if (content.getClass().isInstance(List.class))
//                        return "Any character except: " + String.join("", (List)content);
//                    return "Any except: " + content.toString();
//                default:
//                    break;
//            }
//            return "Unknown";
//       }
//
//        // Enumeration for the repeat
//        public static final int REP_ONCE = 0;
//        public static final int REP_PERHAPS_ONCE = 1;
//        public static final int REP_ANY_NUMBER = 2;
//        public static final int REP_ANY_NUMBER_GREEDY = 3;
//        public static final int REP_ONE_OR_MORE = 4;
//        public static final int REP_ONE_OR_MORE_GREEDY = 5;
//        public int repetition;
//        public String getRepetition()
//        {
//            if (expression == EXP_DONT_KNOW) return "";
//            
//            switch (repetition) {
//                case REP_ONCE:
//                    return "Once";
//                case REP_PERHAPS_ONCE:
//                    return "Perhaps once";
//                case REP_ANY_NUMBER:
//                    return "Any number";
//                case REP_ANY_NUMBER_GREEDY:
//                    return "Any number (greedy)";
//                case REP_ONE_OR_MORE:
//                    return "One or more times";
//                case REP_ONE_OR_MORE_GREEDY:
//                    return "One or more times (greedy)";
//                default:
//                    break;
//            }
//            return "Unknown";
//        }
//
//        // public static final int POS_ENDS_WITH = -2;
//        public static final int POS_STARTS_WITH = -1;
//        public static final int POS_NEW_ROW = Integer.MAX_VALUE - 1;
//        public static final int POS_ENDS_WITH = Integer.MAX_VALUE;
//        public int position; // -2 to N
//        public String getPosition()
//        {
//            switch (position) {
//                case POS_STARTS_WITH:
//                    return "START";
//                case POS_NEW_ROW:
//                    return "NEW";
//                case POS_ENDS_WITH:
//                    return "ENDS";
//                default:
//                    break;
//            }
//            return String.valueOf(position);
//        }
//        
//        // Flags
//        public static final int FLAG_ACTIVE = 1; // Set if active
//        public int flags;
//    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jTextField1 = new javax.swing.JTextField();
        jComboBox5 = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jComboBox4 = new javax.swing.JComboBox<>();
        jTextField3 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jEditPanel = new javax.swing.JPanel();
        jComboBox7 = new javax.swing.JComboBox<>();
        jTextField4 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel8 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Line ending"));

        jLabel1.setText("that occurs");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Don't know", "This exact phrase", "A white space charactor", "One of these charactors", "Any numeric charactor", "Any text charactor", "Any charactor except", "Any charactor", " ", " ", " " }));

        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Only once", "Perhaps once", "Any number of times", "Any number of times (greedy)", "Once or more times", "Once or more times (greedy)" }));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4.add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Line begins"));

        jLabel2.setText("that occurs");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Only once", "Perhaps once", "Any number of times", "Any number of times (greedy)", "Once or more times", "Once or more times (greedy)" }));

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Don't know", "This exact phrase", "One of these words", "A white space charactor", "One of these charactors", "Any numeric charactor", "Any text charactor", "Any charactor except", "Any charactor" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextField3)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel4.add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Line contains"));
        jPanel3.setLayout(new java.awt.BorderLayout(11, 11));

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Don't know", "This exact phrase", "One of these words", "A white space charactor", "One of these charactors", "Any numeric charactor", "Any text charactor", "Any charactor except", "Any charactor", " ", " ", " " }));

        jLabel4.setText("that occurs");

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Only once", "Perhaps once", "Any number of times", "Any number of times (greedy)", "Once or more times", "Once or more times (greedy)" }));

        javax.swing.GroupLayout jEditPanelLayout = new javax.swing.GroupLayout(jEditPanel);
        jEditPanel.setLayout(jEditPanelLayout);
        jEditPanelLayout.setHorizontalGroup(
            jEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jEditPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTextField4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox6, 0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );
        jEditPanelLayout.setVerticalGroup(
            jEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jEditPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jEditPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        jPanel3.add(jEditPanel, java.awt.BorderLayout.NORTH);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"START", null, "EDIT"},
                {"1", null, "NEW"},
                {"+", null, null},
                {"END", null, "EDIT"}
            },
            new String [] {
                "#", "Expression", "Action"
            }
        ));
        jTable1.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(jTable1);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jButton4.setText("Move up");

        jButton7.setText("Delete");

        jButton5.setText("Move down");

        jButton6.setText("Insert");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton6)
                    .addComponent(jButton4)
                    .addComponent(jButton5)
                    .addComponent(jButton7))
                .addContainerGap())
        );

        jPanel8Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton4, jButton5, jButton6, jButton7});

        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7)
                .addContainerGap())
        );

        jPanel3.add(jPanel8, java.awt.BorderLayout.EAST);

        jPanel4.add(jPanel3, java.awt.BorderLayout.CENTER);

        add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Resulting Expression"));

        jTextField2.setEditable(false);
        jTextField2.setText("<Expression here>");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel5, java.awt.BorderLayout.SOUTH);

        jButton2.setText("Cancel");

        jButton1.setText("OK");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jButton1, jButton2});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addContainerGap(439, Short.MAX_VALUE))
        );

        add(jPanel6, java.awt.BorderLayout.EAST);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JComboBox<String> jComboBox4;
    private javax.swing.JComboBox<String> jComboBox5;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JPanel jEditPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
