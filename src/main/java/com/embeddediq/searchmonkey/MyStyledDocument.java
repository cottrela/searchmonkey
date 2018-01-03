/*
 * Copyright (C) 2017 cottr
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

import java.awt.Color;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

/**
 *
 * @author cottr
 */
public class MyStyledDocument extends DefaultStyledDocument
{
    public Style nameStyle;
    public Style pathStyle;
    public Style numberStyle;
    public Style linkStyle;
    public MyStyledDocument()
    {
        nameStyle = addStyle("nameStyle", null);
        pathStyle = addStyle("pathStyle", null);
        StyleConstants.setForeground(pathStyle, Color.GREEN);
        StyleConstants.setItalic(pathStyle, true);
        numberStyle = addStyle("numberStyle", null);
        StyleConstants.setBold(numberStyle, true);
        linkStyle = addStyle("linkStyle", null);
        StyleConstants.setForeground(linkStyle, Color.BLUE);
        StyleConstants.setUnderline(linkStyle, true);
    }
}
