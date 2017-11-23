/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.io.*;
import java.nio.file.*;
import static java.nio.file.FileVisitResult.*;
//import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.attribute.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.nio.file.PathMatcher;
//import java.nio.file.SimpleFileVisitor;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author cottr
 */
public class SearchEngine {
    
    /**
     * Create a new search instance
     * 
     */
    public SearchEngine()
    {
    }
    
    public void AddDirectory(PathMatcher folder)
    {
    }
    
    public void Start() throws IOException
    {
        // string 
        Path startingDir = Paths.get("C:\\");
        String pattern = "*.txt";

        GlobPathFinder finder = new GlobPathFinder(pattern);
        Files.walkFileTree(startingDir, finder);
        finder.done();
    }
    
    public void Cancel()
    {

    }

    public class SearchResult {
        
        public String fileName;
        public String fileExtension;
        public String pathName;
        public long fileSize;
        public FileTime lastModified;
        public FileTime lastAccessTime;
        public FileTime creationTime;
        public int fileFlags = 0;
        
        public final int SYMBOLIC_LINK = 0x1;
        
        public SearchResult(Path file, BasicFileAttributes attrs)
        {
            fileName = FilenameUtils.getName(file.toString());
            fileExtension = FilenameUtils.getExtension(fileName);
            pathName = FilenameUtils.getBaseName(file.toString());
            fileSize = attrs.size();
            lastModified = attrs.lastModifiedTime();
            creationTime = attrs.creationTime();
            lastAccessTime = attrs.lastAccessTime();
            if (attrs.isSymbolicLink()) fileFlags |= SYMBOLIC_LINK;
        }
    }

    public class ResultQueue extends ArrayBlockingQueue<SearchResult> {
        public ResultQueue(int size)
        {
            super(size, true); // Create a fair queue
        }
    }
        

    private class PathFinder extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private int numMatches = 0;
        private ContentMatch contentMatch = null;
        private ResultQueue queue;
        private AtomicBoolean cancel;

        public PathFinder(String pattern) {
            matcher = FileSystems.getDefault()
                    .getPathMatcher(pattern);
            
            // TODO - allow result queue to be passed in
            queue = new ResultQueue(200);
            cancel = new AtomicBoolean(false);
        }

        // Compares the glob pattern against
        // the file or directory name.
        private boolean find(Path file) {
            Path name = file.getFileName();
            return name != null && matcher.matches(name);
        }

        // Prints the total number of
        // matches to standard out.
        public void done() {
            System.out.println("Matched: "
                + numMatches + " OR " + queue.size());
        }

        // Invoke the pattern matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file,
                BasicFileAttributes attrs) {
            if (find(file))
            {
                // TODO - Use exception list to skip folders
                if ((contentMatch == null) || contentMatch.CheckContent(file))
                {
                    // Collect matching files
                    queue.add(new SearchResult(file, attrs));
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
            System.err.println(exc);
            return CONTINUE;
        }
    }

    public class GlobPathFinder extends PathFinder {

        public GlobPathFinder(String pattern) {
            super("glob:" + pattern);
        }
    }

    public class RegexPathFinder extends PathFinder {

        public RegexPathFinder(String pattern) {
            super("regex:" + pattern);
        }
    }

    public class ContentMatch {
        final private Pattern regexMatch;
        public ContentMatch(Pattern regex)
        {
            regexMatch = regex;
        }
        public ContentMatch(String pattern) throws PatternSyntaxException
        {
            regexMatch = Pattern.compile(pattern);
        }

        /**
         * Simple file reader with basic matching
         * @param path
         * @return 
        */
        public boolean CheckContent(Path path)
        {
            try (FileReader fileReader = new FileReader(path.toFile())) {
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (regexMatch.matcher(line).find()) {
                        return true; // on first find within string
                    }
                }
            } catch (IOException ex) {
                System.err.println(ex);
                // Logger.getLogger(SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
    }

}
