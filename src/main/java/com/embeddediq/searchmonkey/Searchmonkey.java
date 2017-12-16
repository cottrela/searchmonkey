/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author cottr
 */
public class Searchmonkey extends javax.swing.JFrame implements ActionListener, ListSelectionListener, ThreadCompleteListener, PropertyChangeListener {

    private final String[] iconList = new String[] {
        "/images/searchmonkey-16x16.png",
        "/images/searchmonkey-22x22.png",
        "/images/searchmonkey-24x24.png",
        "/images/searchmonkey-32x32.png",
        "/images/searchmonkey-48x48.png",
        "/images/searchmonkey-96x96.png",
        "/images/searchmonkey-300x300.png",
    };
    /**
     * Creates new form NewMDIApplication
     */
    public Searchmonkey() {
        initComponents();
        
        // Update icon
        ArrayList<Image> imageList = new ArrayList<>();
        for (String fn: iconList)
        {
            imageList.add(new ImageIcon(getClass().getResource(fn)).getImage());
        }
        setIconImages(imageList);
    }
    
    public void addActionListeners()
    {
        // Add listeners after initialised.
        searchEntryPanel1.addActionListener(this);
        searchResultsTable1.addListSelectionListener(this);
    }
    
    SearchWorker searchTask;
    public void Start()
    {
        // Get a copy of the search settings taken from the search panel
        SearchEntry entry = searchEntryPanel1.getSearchRequest();

        // SearchEntry entry, SearchResultsTable table
        searchEntryPanel1.Start();
        searchResultsTable1.clearTable();
        searchTask = new SearchWorker(entry, searchResultsTable1);
        searchTask.addPropertyChangeListener(this);
        searchMatchView1.setContentMatch(entry.containingText); // Update the content match
        searchTask.execute();
    }
    
    public void Stop()
    {
        if (searchTask != null && !searchTask.isCancelled())
        {
            searchTask.cancel(true);
        }
    }
    
    public void Done()
    {
//        searchResultsTable1.stop();
        searchEntryPanel1.Stop();
    }
    
    public void UpdateContent(SearchResult[] results)
    {
        //searchMatchView1.clearContent();
        Path[] paths = new Path[results.length];
        for (int i=0; i<results.length; i++)
        {
            paths[i] = Paths.get(results[i].pathName, results[i].fileName);
        }
        searchMatchView1.UpdateView(paths);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        searchEntryPanel1 = new com.embeddediq.searchmonkey.SearchEntryPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        searchResultsTable1 = new com.embeddediq.searchmonkey.SearchResultsTable();
        searchMatchView1 = new com.embeddediq.searchmonkey.SearchMatchView();
        searchSummary2 = new com.embeddediq.searchmonkey.SearchSummaryPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Searchmonkey 3.0");
        setMinimumSize(new java.awt.Dimension(500, 500));

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel1.add(searchEntryPanel1, java.awt.BorderLayout.NORTH);

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setToolTipText("");
        jSplitPane1.setTopComponent(searchResultsTable1);
        jSplitPane1.setBottomComponent(searchMatchView1);

        jPanel1.add(jSplitPane1, java.awt.BorderLayout.CENTER);
        jPanel1.add(searchSummary2, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        openMenuItem.setMnemonic('o');
        openMenuItem.setText("Open");
        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('s');
        saveMenuItem.setText("Save");
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText("Save As ...");
        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('e');
        editMenu.setText("Edit");

        cutMenuItem.setMnemonic('t');
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('y');
        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('p');
        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('d');
        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        contentMenuItem.setMnemonic('c');
        contentMenuItem.setText("Contents");
        helpMenu.add(contentMenuItem);

        aboutMenuItem.setMnemonic('a');
        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        System.setProperty("sun.java2d.noddraw", Boolean.TRUE.toString()); // Speed up the resize time
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Searchmonkey.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Searchmonkey.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Searchmonkey.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Searchmonkey.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        setDefaultLookAndFeelDecorated(true); // Speed up the resize time
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                Searchmonkey s = new Searchmonkey();
                s.addActionListeners();
                s.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem contentMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private com.embeddediq.searchmonkey.SearchEntryPanel searchEntryPanel1;
    private com.embeddediq.searchmonkey.SearchMatchView searchMatchView1;
    private com.embeddediq.searchmonkey.SearchResultsTable searchResultsTable1;
    private com.embeddediq.searchmonkey.SearchSummaryPanel searchSummary2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent ae) {
        //if (ae.getClass() == searchEntryPanel1.getClass())
        {
            String command = ae.getActionCommand();
            if (command.equals("Start"))
            {
                Start();
            } else if (command.equals("Stop"))
            {
                Stop();
            }
                
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getValueIsAdjusting()) {
            return;
        }

        // Update the contents
        UpdateContent(searchResultsTable1.getSelectedRows());
    }

    @Override
    public void notifyOfThreadComplete() {
        SwingUtilities.invokeLater(() -> {
            Done();
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        String pn = pce.getPropertyName();
        if (pn.equals("state"))
        {
            StateValue sv = (StateValue)pce.getNewValue();
            if (sv.equals(StateValue.STARTED))
            {
                searchSummary2.ShowProgress(true);
                searchSummary2.SetProgress("Searching");
                searchSummary2.SetSearched("");
            }
            else if (sv.equals(StateValue.DONE))
            {
                searchSummary2.ShowProgress(false);
                if (searchTask.isCancelled()) {
                    searchSummary2.SetStatus("Cancelled!");
                } else {
                    try {
                        SearchSummary ss = searchTask.get();
                        searchSummary2.SetStatus("Done");
                        searchSummary2.SetSearched(String.format("Found: %d match%s (%d seconds)", ss.matchFileCount, ss.matchFileCount != 1 ? "es" : "", (ss.endTime - ss.startTime)/1000000000));
                        searchMatchView1.UpdateSummary(ss);
                        // textarea1.
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(Searchmonkey.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Done();
            }
        }
        else if (pn.equals("match"))
        {
            int val = (Integer)pce.getNewValue();
            searchSummary2.SetSearched(String.format("Found: %d match%s", val, val != 1 ? "es" : ""));
        }
    }

}
