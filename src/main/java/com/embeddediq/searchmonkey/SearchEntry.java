/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Date;
import java.util.List;

/**
 *
 * @author cottr
 */
public class SearchEntry {
    public List<Path> lookIn; // Add one or more folders to look in
    public boolean lookInSubFolders; // List of folders vs look in all
    public PathMatcher fileName; // Filename match object
    public ContentMatch containingText; // Text contents
    public Date modifiedBefore; // null is off, otherwise set to max date
    public Date modifiedAfter; // null is off, otherwise set to min date
    public Date createdBefore; // null is off, otherwise set to max date
    public Date createdAfter; // null is off, otherwise set to min date
    public Date accessedBefore; // null is off, otherwise set to max date
    public Date accessedAfter; // null is off, otherwise set to min date
    public long lessThan; // <=0 is off, otherwise smaller than file size in bytes
    public long greaterThan; // <=0 is off, otherwise smaller than file size in bytes
    public Flags flags = new Flags();

    public class Flags {
        public boolean useFilenameRegex;
        public boolean useContentRegex;
        public boolean ignoreHiddenFiles;
        public boolean ignoreHiddenFolders;
        public boolean ignoreDotFiles;
        public boolean ignoreDotFolders;
        public boolean ignoreSymbolicLinks;
        public boolean ignoreSystemFiles;
    }
    
    // List of PREFIX based on glob type
    public final static String PREFIX_GLOB = "glob:";
    public final static String PREFIX_REGEX = "regex:";
}


