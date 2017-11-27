/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import javax.swing.JDialog;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cottr
 */
public class SearchResultsIT {
    
    public SearchResultsIT() {
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

    /**
     * Test of start method, of class SearchResultsTable.
     */
    @Test
    public void testStart() {
        System.out.println("start");
        SearchResultQueue queue = new SearchResultQueue(200);
        int rateMillis = 50;
        JDialog dlg = new JDialog();
        // dlg.start
        SearchResultsTable jPanel = new SearchResultsTable(queue, rateMillis);
        jPanel.start();
        
        Thread.sleep(1000);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of stop method, of class SearchResultsTable.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        SearchResultsTable instance = null;
        instance.stop();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of insertColumn method, of class SearchResultsTable.
     */
    @Test
    public void testInsertColumn() {
        System.out.println("insertColumn");
        int columnIdent = 0;
        int position = 0;
        SearchResultsTable instance = null;
        instance.insertColumn(columnIdent, position);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeColumn method, of class SearchResultsTable.
     */
    @Test
    public void testRemoveColumn() {
        System.out.println("removeColumn");
        int position = 0;
        SearchResultsTable instance = null;
        instance.removeColumn(position);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
