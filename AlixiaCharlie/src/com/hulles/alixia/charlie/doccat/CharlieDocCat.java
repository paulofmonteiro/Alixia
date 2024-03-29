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
package com.hulles.alixia.charlie.doccat;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SharedUtils;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.TrainingParameters;

final public class CharlieDocCat {
	private final static Logger LOGGER = Logger.getLogger("AlixiaCharlie.CharlieDocCat");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	private final DocumentCategorizerME categorizer;
	
	public CharlieDocCat() {
		URL catModelURL;
		DoccatModel catModel;
		ApplicationKeys appKeys;
        String openNLPPath;
        
        appKeys = ApplicationKeys.getInstance();
        openNLPPath = appKeys.getKey(ApplicationKey.OPENNLPPATH);
        try {
            catModelURL = new URL(ApplicationKeys.toURL(openNLPPath + "/mydoccat.bin"));
        } catch (MalformedURLException ex) {
            throw new AlixiaException("Can't create Document Categorizer URL", ex);
        }
		try {
			catModel = new DoccatModel(catModelURL);
		}
		catch (IOException e) {
			throw new AlixiaException("Can't load Document Categorizer model(s)", e);
		}
		categorizer = new DocumentCategorizerME(catModel);
	}
	
	public String categorizeDocument(String[] tokenizedInput) {
		double[] outcomes;
		String bestCategory;
		
		SharedUtils.checkNotNull(tokenizedInput);
		outcomes = categorizer.categorize(tokenizedInput);
		bestCategory = categorizer.getBestCategory(outcomes);
		LOGGER.log(LOGLEVEL, "DocCat Results:");
		LOGGER.log(LOGLEVEL, "Best category: {0}", bestCategory);
		LOGGER.log(LOGLEVEL, categorizer.getAllResults(outcomes));
		// can also do categorizer.getScoreMap and/or categorizer.getSortedScoreMap
		return bestCategory;
	}
	
	public static DoccatModel train(List<DocumentSample> samples) {
		DoccatModel model;
		DoccatFactory factory;
		TrainingParameters parms;
		
		SharedUtils.checkNotNull(samples);
		factory = new DoccatFactory();
		parms = TrainingParameters.defaultParams();
		parms.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(1));
		
		try (ObjectStream<DocumentSample> sampleStream = ObjectStreamUtils.createObjectStream(samples)){
			model = DocumentCategorizerME.train("en", sampleStream, parms, factory);
		} catch (IOException e) {
			throw new AlixiaException("Can't train DocCat model", e);
		}
		return model;
	}
	
	public static void saveModel(DoccatModel model, String modelFileName) {
		
		SharedUtils.checkNotNull(model);
		SharedUtils.checkNotNull(modelFileName);
		try (OutputStream modelOut = new BufferedOutputStream(new FileOutputStream(modelFileName))){
			model.serialize(modelOut);
		} catch (IOException e) {
			throw new AlixiaException("Can't save DocCat model", e);
		}
	}
}
