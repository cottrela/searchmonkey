/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 * @author cottr
 */
public class SearchResultQueue extends ArrayBlockingQueue<SearchResult> {
    public SearchResultQueue(int size)
    {
        super(size, true); // Create a fair queue
    }
}
