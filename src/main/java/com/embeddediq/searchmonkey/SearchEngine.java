/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.io.*;
import static java.lang.System.nanoTime;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cottr
 */
public class SearchEngine {
    
    private final Thread thread;
    
    /**
     * Create a new search instance
     * 
     * @param startingDir
     * @param finder
     */
    //public SearchEngine(Path startingDir, PathFinder finder)
    //{
//        thread = new Thread(new SearchRunnnable(startingDir, finder));
//    }
    
    public SearchEngine(SearchEntry entry, SearchResultQueue queue, AtomicBoolean cancel)
    {
        this.entry = entry;
        finder = new PathFinder(entry.fileName, queue, cancel, entry.containingText);
        thread = new Thread(new SearchRunnnable(entry.lookIn.get(0), finder));
    }

    PathFinder finder;
    SearchEntry entry;
        
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
        private final Path startingDir;
        private final PathFinder finder;
        
        public SearchRunnnable(Path startingDir, PathFinder finder)
        {
            this.startingDir = startingDir;
            this.finder = finder;
        }
        
        @Override
        public void run() {
            // TODO - fix the start/end time issues
            startTime = nanoTime();
            try {
                Files.walkFileTree(startingDir, finder);
            } catch (IOException ex) {
                Logger.getLogger(SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
            endTime = nanoTime(); // We are done!
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
