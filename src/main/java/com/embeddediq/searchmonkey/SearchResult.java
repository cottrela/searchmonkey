/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author cottr
 */
public class SearchResult {
    public SearchResult(Path file, BasicFileAttributes attrs, int matchCount)
    {
        fileName = FilenameUtils.getName(file.toString());
        fileExtension = FilenameUtils.getExtension(fileName);
        pathName = FilenameUtils.getFullPath(file.toString());
        fileSize = attrs.size();
        lastModified = attrs.lastModifiedTime();
        creationTime = attrs.creationTime();
        lastAccessTime = attrs.lastAccessTime();
        fileFlags = 0;
        if (attrs.isSymbolicLink()) fileFlags |= SYMBOLIC_LINK;
        try {
            if (Files.isHidden(file)) fileFlags |= HIDDEN_FILE;
            contentType = Files.probeContentType(file);
        } catch (IOException ex) {
            System.out.println(ex);
            // Logger.getLogger(SearchResult.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.matchCount = matchCount;
    }

    public SearchResult(Path file, BasicFileAttributes attrs)
    {
        this(file, attrs, 0);
    }
    
    public Object get(int column)
    {
        switch (column)
        {
            case FILENAME:
                return fileName;
            case EXTENSION:
                return fileExtension;
            case FOLDER:
                return pathName;
            case SIZE:
                return fileSize;
            case MODIFIED:
                return lastModified;
            case ACCESSED:
                return lastAccessTime;
            case CREATED:
                return creationTime;
            case FLAGS:
                return fileFlags;
            case COUNT:
                return matchCount;
            case CONTENT_TYPE:
                return contentType;
            default:
                break;
        }
        throw new IllegalArgumentException("Column ID out of bounds");
    }
    
    public String fileName;
    public String fileExtension;
    public String pathName;
    public long fileSize;
    public FileTime lastModified;
    public FileTime lastAccessTime;
    public FileTime creationTime;
    public int fileFlags;
    public int matchCount;
    public String contentType;

    // Flags
    public final static int SYMBOLIC_LINK = 0x1;
    public final static int HIDDEN_FILE = 0x2;
    // public final int HIDDEN_FILE = 0x2;

    /**
     *
     */
    public final static String[] COLUMN_NAMES = new String[] {
        "File", // 0
        "Count", // 1
        "Extension", // 2
        "Folder", // 3
        "File size", // 4
        "Last modified", // 5
        "Last accessed", // 6
        "Created", // 7
        "Flags", // 8
        "Content Type", // 9
    };

    public final static Class[] COLUMN_CLASSES = new Class[]  {
        String.class,
        Integer.class,
        String.class,
        String.class,
        Integer.class,
        FileTime.class,
        FileTime.class,
        FileTime.class,
        Integer.class,
        String.class,
    };


    // Forced enumeration of the column names
    public final static int FILENAME = 0;
    public final static int COUNT = 1;
    public final static int EXTENSION = 2;
    public final static int FOLDER = 3;
    public final static int SIZE = 4;
    public final static int MODIFIED = 5;
    public final static int ACCESSED = 6;
    public final static int CREATED = 7;
    public final static int FLAGS = 8;
    public final static int CONTENT_TYPE = 9;

}

