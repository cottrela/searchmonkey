/*
 * Copyright (C) 2018 cottr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.embeddediq.searchmonkey;

import java.util.List;

/**
 *
 * @author cottr
 */
public class RegexExpression 
{
    public RegexExpression()
    {
        this.expression = 0;
        this.position = 0;
        this.flags = 0;                    
    }

    public RegexExpression(int expression, int position)
    {
        this.expression = expression;
        this.position = position;
        this.flags = 0;                    
    }

    public RegexExpression(RegexExpression copy)
    {
        this.expression = copy.expression;
        this.position = copy.position;
        this.flags = copy.flags;
        this.repetition = copy.repetition;
        this.content = copy.content;
    }

    // Enumeration for the expression
    public static final int EXP_DONT_KNOW = 0;
    public static final int EXP_EXACT_PHRASE = 1;
    public static final int EXP_ONE_OF_THESE = 2;
    public static final int EXP_WHITE_SPACE = 3;
    public static final int EXP_NUMERIC = 4;
    public static final int EXP_ALPHA = 5;
    public static final int EXP_ANY_EXCEPT = 6;
    public static final int EXP_ANY_CHAR = 7;
    public int expression;

    // Expression content
    public Object content; // Typically this is a string, but it could be a list too
    public String getExpression()
    {
        switch (expression) {
            case EXP_DONT_KNOW:
                return "Don't know";
            case EXP_EXACT_PHRASE:
                return (String)content;
            case EXP_ONE_OF_THESE:
                if (content.getClass().isInstance(List.class))
                    return String.join(" OR ", (List)content);
                return content.toString();
            case EXP_WHITE_SPACE:
                return "White space";
            case EXP_NUMERIC:
                return "Any number: 0-9";
            case EXP_ALPHA:
                return "Any letter: a-z or A-Z";
            case EXP_ANY_CHAR:
                if (content.getClass().isInstance(List.class))
                    return "Any of these characters: " + String.join("", (List)content);
                return "Any of these chars: " + content.toString();
            case EXP_ANY_EXCEPT:
                if (content.getClass().isInstance(List.class))
                    return "Any character except: " + String.join("", (List)content);
                return "Any except: " + content.toString();
            default:
                break;
        }
        return "Unknown";
   }

    // Enumeration for the repeat
    public static final int REP_ONCE = 0;
    public static final int REP_PERHAPS_ONCE = 1;
    public static final int REP_ANY_NUMBER = 2;
    public static final int REP_ANY_NUMBER_GREEDY = 3;
    public static final int REP_ONE_OR_MORE = 4;
    public static final int REP_ONE_OR_MORE_GREEDY = 5;
    public int repetition;
    public String getRepetition()
    {
        if (expression == EXP_DONT_KNOW) return "";

        switch (repetition) {
            case REP_ONCE:
                return "Once";
            case REP_PERHAPS_ONCE:
                return "Perhaps once";
            case REP_ANY_NUMBER:
                return "Any number";
            case REP_ANY_NUMBER_GREEDY:
                return "Any number (greedy)";
            case REP_ONE_OR_MORE:
                return "One or more times";
            case REP_ONE_OR_MORE_GREEDY:
                return "One or more times (greedy)";
            default:
                break;
        }
        return "Unknown";
    }

    // public static final int POS_ENDS_WITH = -2;
    public static final int POS_STARTS_WITH = -1;
    public static final int POS_NEW_ROW = Integer.MAX_VALUE - 1;
    public static final int POS_ENDS_WITH = Integer.MAX_VALUE;
    public int position; // -2 to N
    public String getPosition()
    {
        switch (position) {
            case POS_STARTS_WITH:
                return "START";
            case POS_NEW_ROW:
                return "NEW";
            case POS_ENDS_WITH:
                return "ENDS";
            default:
                break;
        }
        return String.valueOf(position);
    }

    // Flags
    public static final int FLAG_ACTIVE = 1; // Set if active
    public int flags;
}

