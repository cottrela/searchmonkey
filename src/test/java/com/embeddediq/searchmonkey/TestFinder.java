package com.embeddediq.searchmonkey;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.embeddediq.searchmonkey.SearchEngine;
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void searchAllFiles() throws IOException
    {
        System.out.println("searchAllFiles");
        Path startingDir = Paths.get("C:\\");
        String path_pattern = ".svn";
        String content_pattern = "hello";

        SearchEngine s = new SearchEngine();
        GlobPathFinder finder;
        finder = s.new GlobPathFinder(path_pattern);
        Files.walkFileTree(startingDir, finder);
        finder.done();

        // Regex search
        String path_regex = "[0-9]+(.*)?.doc";
        String content_regex = "hello";
        RegexPathFinder finder2;
        finder2 = s.new RegexPathFinder(path_regex);
        Files.walkFileTree(startingDir, finder2);
        finder2.done();

    }

        // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void searchForContent() throws IOException
    {
        System.out.println("searchForContent");
        Path startingDir = Paths.get("C:\\Users\\cottr\\Documents\\NetBeansProjects\\trunk\\target\\surefire-reports\\com.embeddediq.searchmonkey.SearchEngineTest.txt");

        SearchEngine s = new SearchEngine();
        ContentMatch m = s.new ContentMatch("Error");
        boolean result = m.CheckContent(startingDir);
        if (!result)
        {
            fail("Error! Could not find text!");
        }
        System.out.println("done");
    }
}
