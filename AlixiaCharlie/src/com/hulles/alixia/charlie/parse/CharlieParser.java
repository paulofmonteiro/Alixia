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
package com.hulles.alixia.charlie.parse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SharedUtils;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

final public class CharlieParser {
	private final static Logger LOGGER = Logger.getLogger("AlixiaCharlie.CharlieParser");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	private final SentenceDetectorME sentenceParser;
	private final TokenizerME tokenizer;

	public CharlieParser() {
		URL sentenceModelURL = null;
		URL tokenModelURL = null;
		SentenceModel sentenceModel;
		TokenizerModel tokenizerModel;
		ApplicationKeys appKeys;
        String openNLPPath;
        
        appKeys = ApplicationKeys.getInstance();
        openNLPPath = appKeys.getKey(ApplicationKey.OPENNLPPATH);
        try {
            sentenceModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-sent.bin"));
            tokenModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-token.bin"));
        } catch (MalformedURLException ex) {
            throw new AlixiaException("Can't create Parser URL(s)", ex);
        }
		
		try {
			sentenceModel = new SentenceModel(sentenceModelURL);
			tokenizerModel = new TokenizerModel(tokenModelURL);
		}
		catch (IOException e) {
			throw new AlixiaException("Can't load sentence/tokenizer model(s)", e);
		}
		sentenceParser = new SentenceDetectorME(sentenceModel);
		tokenizer = new TokenizerME(tokenizerModel);
	}
	
	public String[] detectSentences(String input) {
		String[] sentences;
		
		SharedUtils.checkNotNull(input);
		sentences = sentenceParser.sentDetect(input);
		LOGGER.log(LOGLEVEL, "AFTER SENTENCE DETECT:");
		for (String s : sentences) {
			LOGGER.log(LOGLEVEL, "SENTENCE: {0}", s);
		}
		return sentences;
	}
	
	public String[] parseSentenceTokens(String input) {
		String[] tokens;
		
		SharedUtils.checkNotNull(input);
		tokens = tokenizer.tokenize(input);
		for (String s : tokens) {
			LOGGER.log(LOGLEVEL, "TOKEN: {0}", s);
		}
		return tokens;
	}
}
