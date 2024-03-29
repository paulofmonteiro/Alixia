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
package com.hulles.alixia.config.central;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.SecretKey;

import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.Serialization;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.crypto.AlixiaCrypto;
import com.hulles.alixia.crypto.PurdahKeys;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonWriter;

import redis.clients.jedis.Jedis;

public class ImportExport {
	private final static Charset CHARSET = Charset.forName("UTF-8");
	
	public ImportExport() {
		
	}
	
	@SuppressWarnings("resource")
	private static void loadPurdah() {
		SecretKey aesKey = null;
		byte[] purdahKeyBytes;
		byte[] purdah;
		byte[] purdahBytes;
		PurdahKeys purdahKeys;
		JebusPool jebusPool;
		
		jebusPool = JebusHub.getJebusCentral();
		try (Jedis jebus = jebusPool.getResource()) {
			try {
				aesKey = AlixiaCrypto.getAlixiaFileAESKey();
			} catch (Exception e) {
				throw new AlixiaException("ImportExport: can't recover AES key", e);
			}
			purdahKeyBytes = JebusBible.getBytesKey(JebusKey.ALIXIAPURDAHKEY, jebusPool);
			purdah = jebus.get(purdahKeyBytes);
			try {
				purdahBytes = AlixiaCrypto.decrypt(aesKey, purdah);
			} catch (Exception e) {
				throw new AlixiaException("ImportExport: can't decrypt purdah", e);
			}
			try {
				purdahKeys = (PurdahKeys) Serialization.deSerialize(purdahBytes);
			} catch (ClassNotFoundException | IOException e) {
				throw new AlixiaException("ImportExport: can't deserialize purdah", e);
			}
		}
		PurdahKeys.setInstance(purdahKeys);
	}
	
	private static void createAESKey() throws IOException {
		SecretKey aesKey;
		String fileName;
		ApplicationKeys appKeys;
		Boolean create;
		InputStreamReader stdIn;
		
		appKeys = ApplicationKeys.getInstance();
    	fileName = appKeys.getKey(ApplicationKey.SECRETKEYPATH);
		stdIn = new InputStreamReader(System.in);
		try (BufferedReader reader = new BufferedReader(stdIn)) {
			System.out.println("This will create a new Alixia AES key at " + fileName + ".");
			while ((create = getYN(reader,"Are you sure you want to continue? [yN]: ")
					== null)) {}
			if (!create) {			
				System.out.println("AES key not created");
				return;
			}
		}
		try {
			aesKey = AlixiaCrypto.generateAESKey();
			AlixiaCrypto.setAlixiaFileAESKey(aesKey);
		} catch (Exception e) {
			throw new AlixiaException("ImportExport: can't create AES key", e);
		}
		System.out.println("Created a new AES key");
	}
	
	private static void exportPurdah(Path path) throws IOException {
		PurdahKeys purdahKeys;
		Map<String, String> stringMap;
		String key;
		String value;
		
		SharedUtils.checkNotNull(path);
		loadPurdah();
		purdahKeys = PurdahKeys.getInstance();
		stringMap = purdahKeys.getKeyMap();
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			for (Entry<String, String> entry : stringMap.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				writer.write(key);
				writer.write("=");
				writer.write(value);
				writer.newLine();
			}
		}
		System.out.println("Exported " + stringMap.size() + " key/value pairs from PurdahKeys");
	}
	
	private static void importPurdah(Path path) throws IOException {
		PurdahKeys purdahKeys;
		Map<String, String> stringMap;
		String line;
		String[] keyValue;
		
		SharedUtils.checkNotNull(path);
		purdahKeys = new PurdahKeys();
		stringMap = new HashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
		    while ((line = reader.readLine()) != null) {
		    	if (line.isEmpty()) {
		    		continue;
		    	}
		    	if (line.startsWith("#")) {
		    		// it's a comment
		    		continue;
		    	}
		    	keyValue = line.split("=", 2);
		        stringMap.put(keyValue[0], keyValue[1]);
		    }
		}
		if (purdahKeys.setKeyMap(stringMap)) {
			storePurdah(purdahKeys);
			System.out.println("Successfully imported " + stringMap.size() + 
					" key/value pairs into PurdahKeys");
		} else {
			System.err.println("Error(s) while importing key/value pairs into PurdahKeys");
			System.err.println("PurdahKeys not updated");
		}
	}

	@SuppressWarnings("resource")
	private static void storePurdah(PurdahKeys purdahKeys) {
		JebusPool jebusPool;
		SecretKey aesKey = null;
		byte[] purdah;
		byte[] purdahBytes;
		byte[] purdahKeyBytes;

		SharedUtils.checkNotNull(purdahKeys);
		jebusPool = JebusHub.getJebusCentral();
		purdahKeyBytes = JebusBible.getBytesKey(JebusKey.ALIXIAPURDAHKEY, jebusPool);
		try {
			aesKey = AlixiaCrypto.getAlixiaFileAESKey();
		} catch (Exception e) {
			throw new AlixiaException("ImportExport: can't recover AES key", e);
		}
		try {
			purdahBytes = Serialization.serialize(purdahKeys);
		} catch (IOException e) {
			throw new AlixiaException("ImportExport: can't serialize purdah", e);
		}
		try {
			purdah = AlixiaCrypto.encrypt(aesKey, purdahBytes);
		} catch (Exception e) {
			throw new AlixiaException("ImportExport: can't encrypt purdah", e);
		}
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.set(purdahKeyBytes, purdah);
		}
	}
	
	private static void exportAppKeys(Path path) throws IOException {
		ApplicationKeys appKeys;
		Map<String, String> stringMap;
		String key;
		String value;
		
		SharedUtils.checkNotNull(path);
		appKeys = ApplicationKeys.getInstance();
		stringMap = appKeys.getKeyMap();
		try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
			for (Entry<String, String> entry : stringMap.entrySet()) {
				key = entry.getKey();
				value = entry.getValue();
				writer.write(key);
				writer.write("=");
				writer.write(value);
				writer.newLine();
			}
		}
		System.out.println("Exported " + stringMap.size() + " key/value pairs from ApplicationKeys");
	}
	
	private static void importAppKeys(Path path) throws IOException {
		ApplicationKeys appKeys;
		Map<String, String> stringMap;
		String line;
		String[] keyValue;
		
		SharedUtils.checkNotNull(path);
		appKeys = new ApplicationKeys();
		stringMap = new HashMap<>();
		try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
		    while ((line = reader.readLine()) != null) {
		    	if (line.isEmpty()) {
		    		continue;
		    	}
		    	if (line.startsWith("#")) {
		    		// it's a comment
		    		continue;
		    	}
		    	keyValue = line.split("=", 2);
		        stringMap.put(keyValue[0], keyValue[1]);
		    }
		}
		if (appKeys.setKeyMap(stringMap)) {
			storeAppKeys(appKeys);
			System.out.println("Successfully imported " + stringMap.size() + 
					" key/value pairs into ApplicationKeys");
		} else {
			System.err.println("Error(s) while importing key/value pairs into ApplicationKeys");
			System.err.println("ApplicationKeys not updated");
		}
	}
	
	@SuppressWarnings("resource")
	private static void storeAppKeys(ApplicationKeys appKeys) {
		JebusPool jebusPool;
		byte[] appBytes;
		byte[] appKeyBytes;
        String jsonAppKeys;
        String jsonKey;
        
		SharedUtils.checkNotNull(appKeys);
		jebusPool = JebusHub.getJebusCentral();
        // we also store a JSON version of the ApplicationKeys values
		appKeyBytes = JebusBible.getBytesKey(JebusKey.ALIXIAAPPSKEY, jebusPool);
        jsonAppKeys = createJSONKeys(appKeys);
		jsonKey = JebusBible.getStringKey(JebusKey.ALIXIAJSONAPPSKEY, jebusPool);
		try {
			appBytes = Serialization.serialize(appKeys);
		} catch (IOException e) {
			throw new AlixiaException("ImportExport: can't serialize app keys", e);
		}
		try (Jedis jebus = jebusPool.getResource()) {
			jebus.set(appKeyBytes, appBytes);
            jebus.set(jsonKey, jsonAppKeys);
		}
	}
    
    private static String createJSONKeys(ApplicationKeys appKeys) {
        JsonArrayBuilder builder;
        JsonBuilderFactory factory;
        Map<String, String> keyMap;
        JsonArray jsonKeys;
        JsonWriter jsonWriter;
        StringWriter writer;
        
        keyMap = appKeys.getKeyMap();
        factory = Json.createBuilderFactory(null);
        builder = factory.createArrayBuilder();
		for (Entry<String, String> entry : keyMap.entrySet()) {
            builder.add(factory.createObjectBuilder()
                .add("Name", entry.getKey())
                .add("Value", entry.getValue()));
		}
        jsonKeys = builder.build();
        writer = new StringWriter();
        jsonWriter = Json.createWriter(writer);
        jsonWriter.writeArray(jsonKeys);
        jsonWriter.close();
        return writer.toString();
   }
	
	static void importExport() {
		InputStreamReader stdIn;
		Boolean exportPurdah;
		Boolean importPurdah;
		Boolean exportAppKeys;
		Boolean importAppKeys;
		Boolean createAESKey;
		Path path;
		
		stdIn = new InputStreamReader(System.in);
		try (BufferedReader reader = new BufferedReader(stdIn)) {
			while ((createAESKey = getYN(reader,"Do you want to create a new AES key? [yN]: ")) == null) {}
			if (createAESKey) {
				createAESKey();
				return;
			}
			while ((exportPurdah = getYN(reader,"Do you want to export Purdah? [yN]: ")) == null) {}
			if (exportPurdah) {
				path = getPath(reader, true);
				exportPurdah(path);
				return;
			}
			while ((exportAppKeys = getYN(reader,"Do you want to export ApplicationKeys? [yN]: ")) == null) {}
			if (exportAppKeys) {
				path = getPath(reader, true);
				exportAppKeys(path);
				return;
			}
			while ((importPurdah = getYN(reader,"Do you want to import Purdah? [yN]: ")) == null) {}
			if (importPurdah) {
				path = getPath(reader, false);
				importPurdah(path);
				return;
			}
			while ((importAppKeys = getYN(reader,"Do you want to import ApplicationKeys? [yN]: ")) == null) {}
			if (importAppKeys) {
				path = getPath(reader, false);
				importAppKeys(path);
			}
		} catch (IOException e) {
			System.err.println("System error: I/O error, exiting");
			System.exit(1);
		}
		
	}
	
	private static Path getPath(BufferedReader reader, boolean writing) throws IOException {
		String fn;
		Path path = null;
		Boolean overwrite;
		
		fn = null;
		while (fn == null) {
			System.out.print("Enter the file name ");
			System.out.print(writing ? "to export to: " : "to import from: ");
			fn = reader.readLine();
			if (fn == null) {
				continue;
			}
			path = Paths.get(fn);
			if (writing && Files.exists(path)) {
				while ((overwrite = getYN(reader,"That file exists, do you want to overwrite it? [yN]: ")
						== null)) {}
				if (!overwrite) {
					fn = null;
					continue;
				}
			}
			if (!writing && !Files.exists(path)) {
				System.out.println("That file doesn't exist");
				fn = null;
			}
		}
		return path;
	}
	
	private static Boolean getYN(BufferedReader reader, String prompt) throws IOException {
		String input;
		String ucInput;
		
		System.out.print(prompt);
		input = reader.readLine();
		System.out.println();
		if (input == null) {
			ucInput = "N";
		} else {
			ucInput = input.toUpperCase();
		}
		if (ucInput.startsWith("Y")) {
			return true;
		}
		if (ucInput.startsWith("N")) {
			return false;
		}
		System.out.println("Please type Y or N, or hit Enter for default");
		return null;
	}
}
