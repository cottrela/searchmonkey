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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    public ContentMatch(String pattern) throws PatternSyntaxException
    {
        regexMatch = Pattern.compile(pattern);
    }

    /**
     * Simple file reader with basic matching
     * @param path
     * @return 
    */
    public int CheckContent(Path path)
    {
        int count = 0;
        try (FileReader fileReader = new FileReader(path.toFile())) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (regexMatch.matcher(line).find()) {
                    count ++;
                    // return true; // on first find within string
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
            // Logger.getLogger(SearchEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return count;
    }
    
    public MatchResult getMatch(String line)
    {
        Matcher match = regexMatch.matcher(line);
        if (match.find())
        {
            return match.toMatchResult();
        }
        return null;
    }
}

