/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 *
 * @author cottr
 */
public class PathFinder extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;
    private final SearchResultQueue queue;
    private final AtomicBoolean cancel;
    private ContentMatch contentMatch;

    public PathFinder(PathMatcher path, SearchResultQueue queue, AtomicBoolean cancel, ContentMatch match) {
        this.matcher = path;

        // Allow result queue to be passed in
        this.queue = queue; // new SearchResultQueue(200);
        this.cancel = cancel; // new AtomicBoolean(false);
        this.contentMatch = match;
    }

    public PathFinder(PathMatcher path, SearchResultQueue queue, AtomicBoolean cancel) {
        this(path, queue, cancel, null);
    }

    public PathFinder(String globPath, SearchResultQueue queue, AtomicBoolean cancel) {
        this(FileSystems.getDefault().getPathMatcher("glob:" + globPath), queue, cancel);
    }

    public PathFinder(Pattern regexPath, SearchResultQueue queue, AtomicBoolean cancel) {
        this(FileSystems.getDefault().getPathMatcher("regex:" + regexPath.pattern()), queue, cancel);
    }

    public PathFinder(String globPath, SearchResultQueue queue, AtomicBoolean cancel, ContentMatch match) {
        this(FileSystems.getDefault().getPathMatcher("glob:" + globPath), queue, cancel, match);
    }

    public PathFinder(Pattern regexPath, SearchResultQueue queue, AtomicBoolean cancel, ContentMatch match) {
        this(FileSystems.getDefault().getPathMatcher("regex:" + regexPath.pattern()), queue, cancel, match);
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
                queue.add(result);
            }
        }
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
            BasicFileAttributes attrs) {
        find(dir);
        // TODO - Use exception list to skip folders
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
        


