/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.nio.file.attribute.FileTime;

/**
 *
 * @author cottr
 */
public class SearchSummary {
    public long startTime;
    public long endTime;
    public int matchFileCount;
    public long totalContentMatch;
    public long minContentMatch = -1;
    public long maxContentMatch = -1;
    public int totalFiles; // Number of files checked    
    public int totalFolders; // Number of folders checked
    public int skippedFolders; // Due to IOException
    public int skippedFiles; // Due to IOException
    public long totalMatchBytes; // Size of files that matched (in bytes)
    public long minMatchBytes = -1; // Size of files that matched (in bytes)
    public long maxMatchBytes = -1; // Size of files that matched (in bytes)
    public FileTime firstModified;
    public FileTime lastModified;
    public FileTime firstCreated;
    public FileTime lastCreated;
    public FileTime firstAccessed;
    public FileTime lastAccessed;
}
