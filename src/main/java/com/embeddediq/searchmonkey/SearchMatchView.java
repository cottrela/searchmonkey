/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import javax.swing.Timer;

/**
 *
 * @author adam
 */
public class SearchMatchView extends javax.swing.JPanel implements ActionListener {

    /**
     * Creates new form SearchMatchView
     */
    public SearchMatchView() {
        initComponents();

        // Disable auto-scrolling on the text window
        DefaultCaret caret = (DefaultCaret) jTextPane1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        
        doc = new MyStyledDocument();
        jTextPane1.setDocument(doc);

        // Create delay
        int delayMs = 250;
        timer = new Timer(delayMs, this);
        timer.setInitialDelay(delayMs);
        timer.setRepeats(false);
        timer.setCoalesce(true);
    }

    MyStyledDocument doc;

    /*
    @Override
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals("state"))
        {
            StateValue sv = (StateValue)pce.getNewValue();
            if (sv.equals(StateValue.DONE))
            {
                task = null; // This must be cleared before we can we start the task
                if (busy.get() == 2)
                {
                    timer.restart();
                }
                busy.set(0);
            }
        }
    }
    */

    public class MyStyledDocument extends DefaultStyledDocument
    {
        public Style nameStyle;
        public Style pathStyle;
        public Style numberStyle;
        public Style linkStyle;
        public MyStyledDocument()
        {
            nameStyle = addStyle("nameStyle", null);
            pathStyle = addStyle("pathStyle", null);
            StyleConstants.setForeground(pathStyle, Color.GREEN);
            StyleConstants.setItalic(pathStyle, true);
            numberStyle = addStyle("numberStyle", null);
            StyleConstants.setBold(numberStyle, true);
            linkStyle = addStyle("linkStyle", null);
            StyleConstants.setForeground(linkStyle, Color.BLUE);
            StyleConstants.setUnderline(linkStyle, true);
        }
    }
     
    private ContentMatch match;

    public void setContentMatch(ContentMatch match) {
        this.match = match;
        this.jTextArea1.setText("");
    }
    public ContentMatch getContentMatch() {
        return this.match;
    }

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

    
    private static final String[] FSIZE = new String[] {
        "Bytes",
        "KBytes",
        "MBytes",
        "GBytes",
        "TBytes",
        "PBytes",
    };
    public void UpdateSummary(SearchSummary ss, boolean interim)
    {
        jTextArea1.setText(""); // Clear before entering
        if (interim)
        {
            jTextArea1.append(String.format("Search in progress...\n\n"));
        } else {
            jTextArea1.append(String.format("Search completed in %d seconds.\n\n", (ss.endTime - ss.startTime)/1000000000));
        }
        
        
        jTextArea1.append(String.format("Matched: %d file%s\n", ss.matchFileCount, ss.matchFileCount != 1 ? "s" : ""));
        if (ss.totalMatchBytes > 0)
        {
            int order = getFileOrder(ss.totalMatchBytes);
            double divider = Math.pow(1024, order);
            
            jTextArea1.append(String.format("Size on disk: %.1f %s\n", ((double)ss.totalMatchBytes / divider), FSIZE[order]));
        }
        if (ss.totalContentMatch > 0)
        {
            jTextArea1.append(String.format("Content found: %d hit%s\n", ss.totalContentMatch, ss.totalContentMatch != 1 ? "s" : ""));
        }
        if (ss.skippedFolders > 0)
        {
            jTextArea1.append(String.format("Skipped: %d folder%s\n", ss.skippedFolders, ss.skippedFolders != 1 ? "s" : ""));
        }
        if (ss.skippedFiles > 0)
        {
            jTextArea1.append(String.format("Skipped: %d file%s\n", ss.skippedFiles, ss.skippedFiles != 1 ? "s" : ""));
        }        
    }
            
    public class MatchResult2 {
        public MatchResult2(String title)
        {
            this.title = title;
            isTitle = true;
        }

        public MatchResult2(int line_nr, String title, List<MatchResult> results)
        {
            this.title = title;
            this.line_nr = line_nr;
            this.results = results;
            isTitle = false;
        }
        
        public String title;
        public boolean isTitle = false;
        public int line_nr;
        public List<MatchResult> results;
        public int start; // List of start/end results
        public int end; // List of start/end results
    }

    public class ViewUpdate extends SwingWorker<MyStyledDocument, MatchResult2> { 
        Path[] paths;
        public ViewUpdate(Path[] paths)
        {
            super();
            this.paths = paths;
        }
        

        @Override
        protected MyStyledDocument doInBackground() {            
            MyStyledDocument previewDoc = new MyStyledDocument();
            for (Path path: paths)
            {
                if (isCancelled()) break; // Check for cancel
                consumePath(path, previewDoc);
            }
            return previewDoc;
        }

        @Override
        protected void done() {
            if (isCancelled()) return; // Ignore cancelled
            try {
                MyStyledDocument doc2 = get();
                jTextPane2.setDocument(doc2);
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(SearchMatchView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        private void consumePath(Path path, MyStyledDocument previewDoc)
        {
            try {
                LineIterator lineIterator = FileUtils.lineIterator(path.toFile());
                try
                {
                    publish(new MatchResult2(path.toString() + "\n"));
                    previewDoc.insertString(previewDoc.getLength(), path.toString() + "\n", previewDoc.linkStyle);
                    int i = 0;
                    while (lineIterator.hasNext())
                    {
                        if (isCancelled()) break; // Check for cancel
                        String line = lineIterator.nextLine();
                        i ++;
                        if (match != null)
                        {
                            List<MatchResult> results = match.getMatches(line);
                            if (results.size() > 0)
                            {
                                publish(new MatchResult2(i, line, results));
                            }
                        }
                        previewDoc.insertString(previewDoc.getLength(), line + "\n", previewDoc.nameStyle);
                    }
                    // return resultList;
                }
                catch (BadLocationException ex) {
                    Logger.getLogger(SearchMatchView.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    lineIterator.close();
                }
            }
            catch (IOException er)
            {
                Logger.getLogger(SearchMatchView.class.getName()).log(Level.SEVERE, null, er);
            }
        }

                
        /**
         *
         * @param results
         */
        @Override
        public void process(List<MatchResult2> results)
        {
            try {
                for (MatchResult2 result: results)
                {
                    if (isCancelled()) break; // Check for cancel
                    if (result.isTitle)
                    {
                        doc.insertString(doc.getLength(), result.title, doc.linkStyle);
                    } else {
                        doc.insertString(doc.getLength(), String.format("Line %d:\t", result.line_nr), doc.numberStyle);

                        // Append the match text and format
                        List<MatchResult> resultsx = result.results;
                        String line = result.title;
                        int pos = 0;
                        for (MatchResult res: resultsx)
                        {
                            int s = res.start();
                            int e = res.end();
                            doc.insertString(doc.getLength(), line.substring(pos, s), doc.nameStyle);
                            doc.insertString(doc.getLength(), line.substring(s, e), doc.linkStyle);
                            pos = e;
                        }
                        doc.insertString(doc.getLength(), line.substring(pos) + "\n", doc.nameStyle);
                    }
                }
            } catch (BadLocationException ex) {
                Logger.getLogger(SearchMatchView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private ViewUpdate task;

    private Timer timer;

    public void UpdateView(Path[] paths)
    {
        //if (busy.get() == 1)
        if (task != null && !task.isCancelled())
        {
            try {
                task.cancel(true);
            } catch (CancellationException ex) {
                Logger.getLogger(SearchMatchView.class.getName()).log(Level.SEVERE, null, ex);
            }
            task = null;
            // busy.set(2); // Cancelled, awaiting closure
        }//  else if (busy.get() == 0) {
            // start (or restart) timer
            this.paths = paths;
            timer.restart();
        //}
    }

    // AtomicInteger busy = new AtomicInteger(0);

    private Path[] paths;
    @Override
    public void actionPerformed(ActionEvent ae) {
        // if (busy.get() == 0)
        {
            // Clear the results
            jTextPane1.setText("");
            jTextPane2.setText("");

            // After a short delay, update the hits
            task = new ViewUpdate(paths);
            //task.addPropertyChangeListener(this);
            task.execute();
            // busy.set(1);
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTabbedPane1.addTab("Summary", jScrollPane1);

        jTextPane1.setEditable(false);
        jTextPane1.setToolTipText("");
        jScrollPane3.setViewportView(jTextPane1);

        jTabbedPane1.addTab("Hits", jScrollPane3);

        jTextPane2.setEditable(false);
        jTextPane2.setToolTipText("");
        jScrollPane4.setViewportView(jTextPane2);

        jTabbedPane1.addTab("Preview", jScrollPane4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 523, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 375, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Thumbnails", jPanel2);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 523, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 375, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Reports", jPanel3);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables
}
