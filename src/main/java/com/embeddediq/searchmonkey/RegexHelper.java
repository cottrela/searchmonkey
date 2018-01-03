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
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;

/**
 *
 * @author cottr
 */
public class RegexHelper extends javax.swing.JPanel implements DocumentListener {

    /**
     * Creates new form RegexHelper
     */
    public RegexHelper(int flags) {
        initComponents();
        //Scanner in = new Scanner(new )
        
        this.flags = flags;
        try {
            String fn = "/help/regex.htm";
            URL url = getClass().getResource(fn);
            jTextPane1.setPage(url);
        } catch (IOException ex) {
            Logger.getLogger(RegexHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Create some styles
        MyStyledDocument doc = new MyStyledDocument();
        try {
            doc.insertString(0, jTextPane2.getText(), doc.nameStyle);
        } catch (BadLocationException ex) {
            Logger.getLogger(RegexHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        jTextPane2.setStyledDocument(doc);


        // Add listener
        jTextField1.getDocument().addDocumentListener(this);
        // jTextPane2.getDocument().addDocumentListener(this);
    }
    
    public JButton getAcceptButton()
    {
        return this.jButton1;
    }
    
    public String getRegex()
    {
        return jTextField1.getText();
    }
    
    public void setRegex(String val)
    {
        jTextField1.setText(val);
    }
    
    private Style as;
    private Style def;
    private int flags;
    private void UpdateRegex()
    {
        try
        {
            String txt = jTextField1.getText();
            if (txt.length() == 0) return;
            Pattern compile = Pattern.compile(txt, flags);
            Matcher m = compile.matcher(this.jTextPane2.getText().replaceAll("(?!\\r)\\n", ""));
            MyStyledDocument doc = (MyStyledDocument)this.jTextPane2.getDocument();
            doc.setCharacterAttributes(0, doc.getLength(), doc.nameStyle, true);
            if (m.find())
            {
                this.jTextField1.setBackground(Color.GREEN);
                do{
                    int s = m.start();
                    int e = m.end();
                    doc.setCharacterAttributes(s, e-s, doc.linkStyle, false);
                    //doc.setParagraphAttributes(s, e-s, as, false);
                } while (m.find());
            }
            else
            {
                this.jTextField1.setBackground(Color.ORANGE);
                this.jTextPane2.setSelectionColor(Color.ORANGE);
            }
        }
        catch (IllegalArgumentException ex)
        {
            this.jTextField1.setBackground(Color.RED);
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jButton1 = new javax.swing.JButton();

        jTextPane1.setEditable(false);
        jTextPane1.setText("Construct\tMatches   Characters x\tThe character x \\\\\tThe backslash character \\0n\tThe character with octal value 0n (0 <= n <= 7) \\0nn\tThe character with octal value 0nn (0 <= n <= 7) \\0mnn\tThe character with octal value 0mnn (0 <= m <= 3, 0 <= n <= 7) \\xhh\tThe character with hexadecimal value 0xhh \\uhhhh\tThe character with hexadecimal value 0xhhhh \\x{h...h}\tThe character with hexadecimal value 0xh...h (Character.MIN_CODE_POINT  <= 0xh...h <=  Character.MAX_CODE_POINT) \\t\tThe tab character ('\\u0009') \\n\tThe newline (line feed) character ('\\u000A') \\r\tThe carriage-return character ('\\u000D') \\f\tThe form-feed character ('\\u000C') \\a\tThe alert (bell) character ('\\u0007') \\e\tThe escape character ('\\u001B') \\cx\tThe control character corresponding to x   Character classes [abc]\ta, b, or c (simple class) [^abc]\tAny character except a, b, or c (negation) [a-zA-Z]\ta through z or A through Z, inclusive (range) [a-d[m-p]]\ta through d, or m through p: [a-dm-p] (union) [a-z&&[def]]\td, e, or f (intersection) [a-z&&[^bc]]\ta through z, except for b and c: [ad-z] (subtraction) [a-z&&[^m-p]]\ta through z, and not m through p: [a-lq-z](subtraction)   Predefined character classes .\tAny character (may or may not match line terminators) \\d\tA digit: [0-9] \\D\tA non-digit: [^0-9] \\s\tA whitespace character: [ \\t\\n\\x0B\\f\\r] \\S\tA non-whitespace character: [^\\s] \\w\tA word character: [a-zA-Z_0-9] \\W\tA non-word character: [^\\w]   POSIX character classes (US-ASCII only) \\p{Lower}\tA lower-case alphabetic character: [a-z] \\p{Upper}\tAn upper-case alphabetic character:[A-Z] \\p{ASCII}\tAll ASCII:[\\x00-\\x7F] \\p{Alpha}\tAn alphabetic character:[\\p{Lower}\\p{Upper}] \\p{Digit}\tA decimal digit: [0-9] \\p{Alnum}\tAn alphanumeric character:[\\p{Alpha}\\p{Digit}] \\p{Punct}\tPunctuation: One of !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~ \\p{Graph}\tA visible character: [\\p{Alnum}\\p{Punct}] \\p{Print}\tA printable character: [\\p{Graph}\\x20] \\p{Blank}\tA space or a tab: [ \\t] \\p{Cntrl}\tA control character: [\\x00-\\x1F\\x7F] \\p{XDigit}\tA hexadecimal digit: [0-9a-fA-F] \\p{Space}\tA whitespace character: [ \\t\\n\\x0B\\f\\r]   java.lang.Character classes (simple java character type) \\p{javaLowerCase}\tEquivalent to java.lang.Character.isLowerCase() \\p{javaUpperCase}\tEquivalent to java.lang.Character.isUpperCase() \\p{javaWhitespace}\tEquivalent to java.lang.Character.isWhitespace() \\p{javaMirrored}\tEquivalent to java.lang.Character.isMirrored()   Classes for Unicode scripts, blocks, categories and binary properties \\p{IsLatin}\tA Latin script character (script) \\p{InGreek}\tA character in the Greek block (block) \\p{Lu}\tAn uppercase letter (category) \\p{IsAlphabetic}\tAn alphabetic character (binary property) \\p{Sc}\tA currency symbol \\P{InGreek}\tAny character except one in the Greek block (negation) [\\p{L}&&[^\\p{Lu}]] \tAny letter except an uppercase letter (subtraction)   Boundary matchers ^\tThe beginning of a line $\tThe end of a line \\b\tA word boundary \\B\tA non-word boundary \\A\tThe beginning of the input \\G\tThe end of the previous match \\Z\tThe end of the input but for the final terminator, if any \\z\tThe end of the input   Greedy quantifiers X?\tX, once or not at all X*\tX, zero or more times X+\tX, one or more times X{n}\tX, exactly n times X{n,}\tX, at least n times X{n,m}\tX, at least n but not more than m times   Reluctant quantifiers X??\tX, once or not at all X*?\tX, zero or more times X+?\tX, one or more times X{n}?\tX, exactly n times X{n,}?\tX, at least n times X{n,m}?\tX, at least n but not more than m times   Possessive quantifiers X?+\tX, once or not at all X*+\tX, zero or more times X++\tX, one or more times X{n}+\tX, exactly n times X{n,}+\tX, at least n times X{n,m}+\tX, at least n but not more than m times   Logical operators XY\tX followed by Y X|Y\tEither X or Y (X)\tX, as a capturing group   Back references \\n\tWhatever the nth capturing group matched \\k<name>\tWhatever the named-capturing group \"name\" matched   Quotation \\\tNothing, but quotes the following character \\Q\tNothing, but quotes all characters until \\E \\E\tNothing, but ends quoting started by \\Q   Special constructs (named-capturing and non-capturing) (?<name>X)\tX, as a named-capturing group (?:X)\tX, as a non-capturing group (?idmsuxU-idmsuxU) \tNothing, but turns match flags i d m s u x U on - off (?idmsux-idmsux:X)  \tX, as a non-capturing group with the given flags i d m s u x on - off (?=X)\tX, via zero-width positive lookahead (?!X)\tX, via zero-width negative lookahead (?<=X)\tX, via zero-width positive lookbehind (?<!X)\tX, via zero-width negative lookbehind (?>X)\tX, as an independent, non-capturing group");
        jScrollPane1.setViewportView(jTextPane1);

        jLabel1.setText("Summary of Regular Expression Constructs:");

        jLabel2.setText("Regular-expression:");

        jTextField1.setText("Regex Search");
        jTextField1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTextField1InputMethodTextChanged(evt);
            }
        });

        jLabel3.setText("Test content:");

        jTextPane2.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla nec orci laoreet mauris venenatis malesuada. Sed vel pretium ex. Aliquam quis metus tristique, cursus augue eu, molestie erat. Praesent eu purus erat. Vestibulum placerat arcu at mi feugiat vulputate. Aenean faucibus libero a lectus iaculis semper. Integer eget ante non eros feugiat volutpat at a tellus. Nulla in sollicitudin tellus, nec tempus odio. Donec sagittis velit sed posuere varius. Duis magna leo, vulputate nec sapien non, efficitur euismod odio. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Suspendisse congue justo quis sapien dignissim, vel pellentesque est gravida.");
        jScrollPane3.setViewportView(jTextPane2);

        jButton1.setText("OK");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(0, 108, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTextField1InputMethodTextChanged
        // TODO - add a short delay
        //UpdateRegex();
    }//GEN-LAST:event_jTextField1InputMethodTextChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    // End of variables declaration//GEN-END:variables

    @Override
    public void insertUpdate(DocumentEvent de) {
        // TODO - add a short delay
        UpdateRegex();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        // TODO - add a short delay
        UpdateRegex();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        // TODO - add a short delay
        UpdateRegex();
    }
}
