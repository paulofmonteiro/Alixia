/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.alixia.media.text;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.hulles.alixia.media.MediaUtils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Use a Java Swing window to display the info that Alixia returns with a little
 * pizzazz.
 * 
 * @author hulles
 */
public class SwingTextDisplayer {
	private final static Logger LOGGER = Logger.getLogger("AlixiaMedia.SwingTextDisplayer");
	private final static Level LOGLEVEL = Level.FINE;
	TextScroller textScroller= null;
	private JFrame frame;
    AbstractDocument doc;
    SimpleAttributeSet defaultAttrs;
    final String text;
    final String title;
    private final TextDisplayer caller;
    
	public SwingTextDisplayer(String text, String title, TextDisplayer caller) {
		
		MediaUtils.nullsOkay(text);
		MediaUtils.nullsOkay(title);
        MediaUtils.checkNotNull(caller);
		this.text = text;
		this.title = title;
        this.caller = caller;
        defaultAttrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(defaultAttrs, "SansSerif");
        StyleConstants.setFontSize(defaultAttrs, 14);
	}
    
	public AttributeSet getDefaultAttributeSet() {
	
		return defaultAttrs;
	}
	
	public void appendText(String txt) {
		
		MediaUtils.checkNotNull(txt);
		if (textScroller != null) {
			textScroller.addText(txt);
		}
	}
	public void appendText(String txt, AttributeSet attrs) {
		
		MediaUtils.checkNotNull(txt);
		MediaUtils.checkNotNull(attrs);
		if (textScroller != null) {
			textScroller.addText(txt, attrs);
		}
	}

	public void startup() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
            	TextScroller scroller;
            	
                scroller = createAndShowGUI(text, title);
                textScroller = scroller;
            }
        });
	}
	
	public void shutdown() {
	
		if (frame != null) {
			frame.dispose();
		}
		frame = null;
	}
	
    TextScroller createAndShowGUI(String txt, String ttl) {
    	TextScroller contentPane;
    	
    	MediaUtils.nullsOkay(txt);
    	MediaUtils.nullsOkay(ttl);
    	frame = new JFrame();
    	if (title != null) {
    		frame.setTitle(ttl);
    	}
        frame.addWindowListener(new WindowCloser());
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        contentPane = new TextScroller(txt);
        contentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(contentPane);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        return contentPane;
    }

    private class TextScroller extends JPanel {
		private static final long serialVersionUID = 1L;
		private final JTextPane textPane;

		TextScroller(String text) {
    		super(new BorderLayout());
    		StyledDocument styledDoc;
    		JScrollPane scrollPane;
    		
    		MediaUtils.nullsOkay(text);
            textPane = new JTextPane();
            textPane.setEditable(false);
            textPane.setCaretPosition(0);
            textPane.setMargin(new Insets(5,5,5,5));
            if (text != null) {
                textPane.setText(text);
            }
            styledDoc = textPane.getStyledDocument();
            if (styledDoc instanceof AbstractDocument) {
                doc = (AbstractDocument)styledDoc;
            } else {
                LOGGER.log(Level.SEVERE, "Text pane's document isn't an AbstractDocument!");
                System.exit(-1);
            }
            scrollPane = new JScrollPane(textPane);
            scrollPane.setPreferredSize(new Dimension(600, 600));
            add(scrollPane, BorderLayout.CENTER);
    	}
		
		void addText(String txt) {
			addText(txt, defaultAttrs);
		}
	    void addText(String txt, AttributeSet attrs) {
	    	
	        try {
	        	doc.insertString(doc.getLength(), txt, attrs);
	        } catch (BadLocationException ble) {
	            LOGGER.log(Level.SEVERE, "Couldn't insert text.");
	        }
	    	
	        //Make sure the new text is visible, even if there
	        //was a selection in the text area.
	        textPane.setCaretPosition(textPane.getDocument().getLength());
	    }
    }
	
	private class WindowCloser extends WindowAdapter {

		WindowCloser() {
		}

		@Override
		public void windowClosing(WindowEvent e) {

			LOGGER.log(LOGLEVEL, "Window is closing");
            shutdown();
            caller.textWindowIsClosing();
		}
		
	}
}
