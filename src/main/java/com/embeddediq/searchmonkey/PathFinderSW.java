/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author cottr
 */
public class PathFinderSW extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private final SearchResultQueue queue;
    private final AtomicBoolean cancel;
    private final ContentMatch contentMatch;
    private final SearchEntry entry;
    private final SearchWorker worker;

    public PathFinderSW(SearchEntry entry, SwingWorker worker) {
        this.entry = entry;
        this.worker = (SearchWorker)worker;

        this.contentMatch = entry.containingText;
        this.matcher = entry.fileName;

        // Allow result queue to be passed in
        //this.queue = queue;
        //this.cancel = cancel;
    }

    
    // Compares the glob pattern against
    // the file or directory name.
    private boolean find(Path file) {
        Path name = file.getFileName();
        return name != null && matcher.matches(name);
    }

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
            BasicFileAttributes attrs) {
        if (find(file))
        {
            if (
                    (entry.flags.ignoreSymbolicLinks && attrs.isSymbolicLink()) ||
                    (entry.flags.ignoreHiddenFiles && file.toFile().isHidden()) ||
                    (entry.greaterThan > 0 && (attrs.size() < entry.greaterThan)) ||
                    (entry.lessThan > 0 && (attrs.size() > entry.lessThan)) ||
                    ((entry.modifiedAfter != null) && (attrs.lastModifiedTime().compareTo(entry.modifiedAfter) < 0)) ||
                    ((entry.modifiedBefore != null) && (attrs.lastModifiedTime().compareTo(entry.modifiedBefore) > 0)) ||
                    ((entry.accessedAfter != null) && (attrs.lastAccessTime().compareTo(entry.accessedAfter) < 0)) ||
                    ((entry.accessedBefore != null) && (attrs.lastAccessTime().compareTo(entry.accessedBefore) > 0)) ||
                    ((entry.createdAfter != null) && (attrs.creationTime().compareTo(entry.createdAfter) < 0)) ||
                    ((entry.createdBefore != null) && (attrs.creationTime().compareTo(entry.createdBefore) > 0))
                )
            {
                return CONTINUE;
            }
            
            int count = -1;
            if (contentMatch != null)
            {
                count = contentMatch.CheckContent(file);
            }
            // TODO - Use exception list to skip folders
            if ((contentMatch == null) || (count > 0))
            {
                // Collect matching files
                SearchResult result = new SearchResult(file, attrs, count);
                try {
                    worker.publish();
                    // Blocking call to ensure that we do never overflow..
                    queue.put(result);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PathFinderSW.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        /*
        if (!find(dir))
        {
            return SKIP_SUBTREE;
        }
        */
        // Use exception list to skip entire folders
        if (entry.ignoreFolderSet.contains(dir))
        {
            return SKIP_SUBTREE;
        }
        if (entry.flags.ignoreHiddenFolders && dir.toFile().isHidden())
        {
            return SKIP_SUBTREE;
        }

        if (cancel.get()) return TERMINATE; // user has requested an early exit
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
            IOException exc) {
        // System.err.println(exc);
        return CONTINUE;
    }
}
        


