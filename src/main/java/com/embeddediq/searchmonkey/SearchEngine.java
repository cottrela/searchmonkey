/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.io.*;
import static java.lang.System.nanoTime;
import java.nio.file.*;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 *
 * @author cottr
 */
public class SearchEngine {
    
    private final Thread thread;
    private final SearchRunnnable runable;
    private final PathFinder finder;
    private final SearchEntry entry;    

    // Searchmonkey parent;
    /**
     * Create a new search instance
     * 
     * @param entry
     * @param queue
     * @param cancel
     */
    public SearchEngine(SearchEntry entry, SearchResultQueue queue, AtomicBoolean cancel)
    {
        //this.parent = parent;
        this.entry = entry;
        finder = new PathFinder(entry, queue, cancel);
        runable = new SearchRunnnable(entry.lookIn.get(0), finder);
        thread = new Thread(runable);
    }
    
    public void addThreadCompleteListener(ThreadCompleteListener listener)
    {
        runable.setListener(listener);
    }

    public void start()
    {
        thread.start();
    }
    
    private long startTime = -1; // Not started
    private long endTime = -1; // Not done
    private long getElapsedNanoseconds()
    {
        if (thread.isAlive())
        {
            return nanoTime() - startTime;
        }
        
        // Otherwise thread has stopped (or never started)
        if ((startTime > 0) && (endTime > startTime))
        {
            return endTime - startTime;
        }
            
        return -1;
    }

    public long getElapsedMillis()
    {
        long ns = getElapsedNanoseconds();
        if (ns < 0) return -1;
        return TimeUnit.NANOSECONDS.toMillis(ns);
    }
    
    public long getElapsedSeconds()
    {
        long ns = getElapsedNanoseconds();
        if (ns < 0) return -1;
        return TimeUnit.NANOSECONDS.toSeconds(ns);
    }
    
    private class SearchRunnnable implements Runnable
    {
        private ThreadCompleteListener listener;
        private final Path startingDir;
        private final PathFinder finder;
        
        public SearchRunnnable(Path startingDir, PathFinder finder)
        {
            this.startingDir = startingDir;
            this.finder = finder;
        }

        public void setListener(ThreadCompleteListener listener)
        {
            this.listener = listener;
        }
        
        @Override
        public void run() {
            // TODO - fix the start/end time issues
            try {
                startTime = nanoTime();
                if (entry.flags.lookInSubFolders) {
                    Files.walkFileTree(startingDir, finder);
                } else {
                    Files.walkFileTree(startingDir, new HashSet<>(), 0, finder);
                }
                endTime = nanoTime(); // We are done!
            } catch (IOException ex) {
                Logger.getLogger(SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                listener.notifyOfThreadComplete();
            }
        }
    }
        /*
public void Start() throws IOException
    {
        startTime = nanoTime();
        Files.walkFileTree(startingDir, finder);
        endTime = nanoTime(); // We are done!
    }
*/
   
    
}
