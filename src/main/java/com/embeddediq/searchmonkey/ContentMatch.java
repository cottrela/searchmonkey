/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.embeddediq.searchmonkey;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author cottr
 */
public class ContentMatch {
    final private Pattern regexMatch;
    public ContentMatch(Pattern regex)
    {
        regexMatch = regex;
    }
    /*
    public ContentMatch(String pattern) throws PatternSyntaxException
    {
        regexMatch = Pattern.compile(pattern);
    }*/

    /**
     * Simple file reader with basic matching
     * @param path
     * @return 
    */
    public int CheckContent(Path path)
    {
        int count = 0;
        try{
            FileReader fileReader = new FileReader(path.toFile());
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                count += getMatchCount(line);
            }
        } catch (IOException er) {
            // Logger.getLogger(ContentMatch.class.getName()).log(Level.SEVERE, null, er);
        }
        return count;
    }

    private int getMatchCount(String line)
    {
        Matcher match = regexMatch.matcher(line);
        int result = 0;
        while (match.find())
        {
            result++;
        }
        return result;
    }

    public List<MatchResult> getMatches(String line)
    {
        Matcher match = regexMatch.matcher(line);
        List<MatchResult> results = new ArrayList<>();
        while (match.find())
        {
            results.add(match.toMatchResult());
        }
        return results;
    }
}

