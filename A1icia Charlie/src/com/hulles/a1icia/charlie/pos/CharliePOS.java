/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
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
 *******************************************************************************/
package com.hulles.a1icia.charlie.pos;

import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.ApplicationKeys;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;
import java.net.MalformedURLException;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

final public class CharliePOS {
	private final static Logger LOGGER = Logger.getLogger("A1iciaCharlie.CharliePOS");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private final POSTaggerME posTagger;

	public CharliePOS() {
		URL posModelURL;
		POSModel posModel;
		ApplicationKeys appKeys;
        
        appKeys = ApplicationKeys.getInstance();
        String openNLPPath = appKeys.getOpenNLPPath();
        try {
            posModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/en-pos-maxent.bin"));
        } catch (MalformedURLException ex) {
            throw new A1iciaException("Can't create POS URL", ex);
        }
		
		try {
			posModel = new POSModel(posModelURL);
		}
		catch (IOException e) {
			throw new A1iciaException("Can't load POS model(s)", e);
		}
		posTagger = new POSTaggerME(posModel);
	}
	
	public String[] generatePOS(String[] tokenizedInput) {
		String[] posStrings;
		
		A1iciaUtils.checkNotNull(tokenizedInput);
		posStrings = posTagger.tag(tokenizedInput);
		for (String s : posStrings) {
			LOGGER.log(LOGLEVEL, "POS: {0}", s);
		}
		return posStrings;
	}
}