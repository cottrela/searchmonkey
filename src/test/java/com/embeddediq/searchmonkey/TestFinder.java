package com.embeddediq.searchmonkey;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.embeddediq.searchmonkey.SearchEngine.*;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 *
 * @author cottr
 */
public class TestFinder {
    
    public TestFinder() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void searchForContent() throws IOException
    {
        System.out.println("searchForContent");
        Path startingDir = Paths.get("C:\\Users\\cottr\\Documents\\NetBeansProjects\\trunk\\target\\surefire-reports\\com.embeddediq.searchmonkey.SearchEngineTest.txt");

        // SearchEngine s = new SearchEngine();
        ContentMatch m = new ContentMatch("Error");
        int result = m.CheckContent(startingDir);
        if (result == 0)
        {
            fail("Error! Could not find text!");
        }
        System.out.println("done");
    }

    @Test
    public void searchAllFiles() throws IOException
    {
        System.out.println("searchAllFiles");
        
        Path startingDir = Paths.get("C:\\");
        String path_pattern = "*.{doc,docx}";
        String content_pattern = "a";
        
        SearchResultQueue queue = new SearchResultQueue(200);
        AtomicBoolean cancel = new AtomicBoolean(false);

        //SearchEngine s = new SearchEngine();
        PathFinder finder;
        finder = new PathFinder(path_pattern, queue, cancel, new ContentMatch(content_pattern));
        Files.walkFileTree(startingDir, finder);

        // Regex search
        String path_regex = "[0-9]+(.*)?.doc";
        String content_regex = "a";
        PathFinder finder2;
        finder2 = new PathFinder(Pattern.compile(path_regex), queue, cancel, new ContentMatch(Pattern.compile(content_regex)));
        Files.walkFileTree(startingDir, finder2);

        // Display all results
        queue.forEach((r) -> System.out.println(r.fileName));
        queue.clear();
    }
}
