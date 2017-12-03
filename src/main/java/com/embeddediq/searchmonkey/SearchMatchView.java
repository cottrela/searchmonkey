/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.MinimalHTMLWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

/**
 *
 * @author adam
 */
public class SearchMatchView extends javax.swing.JPanel {

    /**
     * Creates new form SearchMatchView
     */
    public SearchMatchView() {
        initComponents();

        int count = 10000;
        
        // TODO - move to the designer
        // TODO - if possible to do so...
        // jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        DefaultCaret caret = (DefaultCaret) jTextPane1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        
        doc = (StyledDocument) jTextPane1.getDocument();

        nameStyle = doc.addStyle("nameStyle", null);
        pathStyle = doc.addStyle("pathStyle", null);
        StyleConstants.setForeground(pathStyle, Color.GREEN);
        StyleConstants.setItalic(pathStyle, true);
        numberStyle = doc.addStyle("numberStyle", null);
        StyleConstants.setBold(numberStyle, true);
        linkStyle = doc.addStyle("linkStyle", null);
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        StyleConstants.setUnderline(linkStyle, true);
        
        queue = new ArrayBlockingQueue<>(count);
        task = new ViewUpdate(queue);
        task.execute();
    }
    BlockingQueue<Path> queue;
    StyledDocument doc;
    Style nameStyle;
    Style pathStyle;
    Style numberStyle;
    Style linkStyle;

    private ContentMatch match;

    public void setContentMatch(ContentMatch match) {
        this.match = match;
    }
    public ContentMatch getContentMatch() {
        return this.match;
    }
    
    public void clearContent()
    {
        //queue.clear();
        //task.cancel(true);
        //task.get();
        // queue.add(path);
        jTextPane1.setText(""); // Clear all
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
        // public List<String> results;
        public int start; // List of start/end results
        public int end; // List of start/end results
    }

    public class ViewUpdate extends SwingWorker<ArrayList<MatchResult2>, MatchResult2> { 

        private BlockingQueue<Path> pathQueue;
        public ViewUpdate(BlockingQueue<Path> pathQueue)
        {
            super();
            this.pathQueue = pathQueue;
        }
        
        @Override
        protected ArrayList<MatchResult2> doInBackground() {            
            try{
                while(true)
                {
                    consumePath(pathQueue.take());
                }
            }
            catch (InterruptedException ex)
            {
                System.out.println(ex);
            }
            return new ArrayList<>();
        }

        private void consumePath(Path path)
        {
            // ArrayList<MatchResult2> resultList = new ArrayList<>();
            try {
                LineIterator lineIterator = FileUtils.lineIterator(path.toFile());
                try
                {
                    publish(new MatchResult2(path.toString() + "\n"));
                    int i = 0;
                    while (lineIterator.hasNext())
                    {
                        if (isCancelled()) break; // Check for cancel
                        String line = lineIterator.nextLine();
                        i ++;
                        List<MatchResult> results = match.getMatches(line);
                        if (results.size() > 0)
                        {
                            publish(new MatchResult2(i, line, results));
                        }
                    }
                    // return resultList;
                }
                finally {
                    lineIterator.close();
                }
            }
            catch (IOException er)
            {
                System.out.println(er);
                // return null;
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
                    if (result.isTitle)
                    {
                        doc.insertString(doc.getLength(), result.title, linkStyle);
                    } else {
                        doc.insertString(doc.getLength(), String.format("Line %d:\t", result.line_nr), numberStyle);

                        // Append the match text and format
                        List<MatchResult> resultsx = result.results;
                        String line = result.title;
                        int pos = 0;
                        for (MatchResult res: resultsx)
                        {
                            int s = res.start();
                            int e = res.end();
                            doc.insertString(doc.getLength(), line.substring(pos, s), nameStyle);
                            doc.insertString(doc.getLength(), line.substring(s, e), linkStyle);
                            pos = e;
                        }
                        doc.insertString(doc.getLength(), line.substring(pos) + "\n", nameStyle);
                    }
                }
            } catch (BadLocationException ex) {
                System.out.println(ex);
                // Logger.getLogger(SearchMatchView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    ViewUpdate task;
    public void UpdateView(Path path)
    {
        queue.add(path);
        /*
        if (task != null && task.cancel(true))
        {
            try {
                ArrayList<MatchResult2> get = task.get();
                System.out.println(get);
            } catch (InterruptedException | ExecutionException | CancellationException ex) {
                System.out.println(ex);
                // Logger.getLogger(SearchMatchView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        */
    }
    

    /**
     *
     * @param path
     * @throws IOException
     */
//    public void UpdateView2(Path path) throws IOException
//    {
//        // jTextPane1.setContentType("text/html");
//        jTextPane1.setEditable(false);
//        StyledDocument doc = (StyledDocument) jTextPane1.getDocument();
//
//        Style nameStyle = doc.addStyle("nameStyle", null);
//        Style pathStyle = doc.addStyle("pathStyle", null);
//        StyleConstants.setForeground(pathStyle, Color.GREEN);
//        StyleConstants.setItalic(pathStyle, true);
//        Style numberStyle = doc.addStyle("numberStyle", null);
//        StyleConstants.setBold(numberStyle, true);
//        Style linkStyle = doc.addStyle("linkStyle", null);
//        StyleConstants.setForeground(linkStyle, Color.BLUE);
//        StyleConstants.setUnderline(linkStyle, true);
//
//        LineIterator lineIterator = FileUtils.lineIterator(path.toFile());
//        try
//        {
//            doc.insertString(doc.getLength(), path.toString() + "\n", pathStyle);
//            int i = 0;
//            while (lineIterator.hasNext())
//            {
//                String line = lineIterator.nextLine();
//                i ++;
//                MatchResult2 res = match.getMatch(line);
//                if (res != null)
//                {
//                    // Append string
//                    doc.insertString(doc.getLength(), String.format("Line %d:\t", i), numberStyle);
//
//                    // Append the match text and format
//                    int s = res.start();
//                    int e = res.end();
//                    doc.insertString(doc.getLength(), line.substring(0, s), nameStyle);
//                    doc.insertString(doc.getLength(), line.substring(s, e), linkStyle);
//                    doc.insertString(doc.getLength(), line.substring(e) + "\n", nameStyle);
//                }
//            }
//        }
//        catch (BadLocationException ex)
//        {
//            System.out.println(ex);
//        }
//        finally {
//            lineIterator.close();
//        }
//    }
        
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        jScrollPane3.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
