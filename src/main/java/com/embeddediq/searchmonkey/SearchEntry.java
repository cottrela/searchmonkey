/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author cottr
 */
public class SearchEntry {
    public List<Path> lookIn; // Add one or more folders to look in
    public boolean lookInSubFolders; // List of folders vs look in all
    public PathMatcher fileName; // Filename match object
    public ContentMatch containingText; // Text contents
    public FileTime modifiedBefore; // null is off, otherwise set to max date
    public FileTime modifiedAfter; // null is off, otherwise set to min date
    public FileTime createdBefore; // null is off, otherwise set to max date
    public FileTime createdAfter; // null is off, otherwise set to min date
    public FileTime accessedBefore; // null is off, otherwise set to max date
    public FileTime accessedAfter; // null is off, otherwise set to min date
    public long lessThan; // <=0 is off, otherwise smaller than file size in bytes
    public long greaterThan; // <=0 is off, otherwise smaller than file size in bytes
    public Flags flags = new Flags();
    public Set<Path> ignoreFolderSet = new HashSet<>(); // Set of paths to ignore

    public class Flags {
        public boolean useFilenameRegex;
        public boolean useContentRegex;
        public boolean ignoreHiddenFiles;
        public boolean ignoreHiddenFolders;
        public boolean ignoreSymbolicLinks;
        public boolean lookInSubFolders;
        public boolean caseInsensitive;
    }
    
    // List of PREFIX based on glob type
    public final static String PREFIX_GLOB = "glob:";
    public final static String PREFIX_REGEX = "regex:";
}


