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
package com.hulles.a1icia.node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import com.eclipsesource.v8.JavaCallback;
import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.shared.A1iciaAPIException;
import com.hulles.a1icia.api.shared.SharedUtils;

public final class A1iciaWebServer {
	private static final String BUNDLE_NAME = "com.hulles.a1icia.node.Version";
//	private static final String MEDIA_URL = "a1icia/services/media?mmd=";
//	private static final boolean USE_TTS = true;
	private final static int PORT = 12349;
	private NodeJS nodeJS;
	
	public A1iciaWebServer() {
		
		if (SharedUtils.alreadyRunning(PORT)) {
			System.out.println("A1iciaWebServer is already running");
			System.exit(1);
		}
		System.out.println(getVersionString());
		System.out.println(A1iciaConstants.getA1iciasWelcome());
		System.out.println();
	}	
	
	// it sucks that we need to do this just to get a file for the j2v8 exec method...
	private File getResourceAsFile(String resourcePath) {
		ClassLoader cl;
		File tempFile;
        int bytesRead;
        byte[] buffer;
		
		cl = this.getClass().getClassLoader();
	    try (InputStream in = cl.getResourceAsStream(resourcePath)) {
	        if (in == null) {
		    	throw new A1iciaAPIException("A1iciaWebServer: can't create stream for JS file");
	        }
	        tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
	        tempFile.deleteOnExit();
	        try (FileOutputStream out = new FileOutputStream(tempFile)) {
	            //copy stream
	            buffer = new byte[1024];
	            while ((bytesRead = in.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	        }
	    } catch (IOException e) {
	    	throw new A1iciaAPIException("A1iciaWebServer: IO exception creating JS file", e);
	    }
        return tempFile;
	}

	/**
	 * 
	 * "All access to a single runtime must be from the same thread." Sigh. It helps to RTFM.
	 * 
	 */
	public void serve() {
		File nodeScript;
		JavaCallback plainCallback;
		JavaCallback htmlCallback;
		JavaCallback registrar;
		V8 runtime;
		    		
		nodeJS = NodeJS.createNodeJS();
		
		plainCallback = new PlainResponder();
		htmlCallback = new HtmlResponder();
		registrar = new Registrar();
		runtime = nodeJS.getRuntime();
		runtime.registerJavaMethod(plainCallback, "plainAnswer");
		runtime.registerJavaMethod(htmlCallback, "htmlAnswer");
		runtime.registerJavaMethod(registrar, "register");
		
		nodeScript = getResourceAsFile("com/hulles/a1icia/node/A1iciaTinyWebSocket.js");
//		nodeScript = getResourceAsFile("com/hulles/a1icia/node/A1iciaTinyServer.js");
//		nodeScript = getResourceAsFile("com/hulles/a1icia/node/A1iciaServer2.js");
		nodeJS.exec(nodeScript);
		
		while(nodeJS.isRunning()) {
			nodeJS.handleMessage();
		}
		nodeJS.release();
	}
	
	private static String getVersionString() {
		ResourceBundle bundle;
		StringBuilder sb;
		String value;
		
		bundle = ResourceBundle.getBundle(BUNDLE_NAME);
		sb = new StringBuilder();
		value = bundle.getString("Name");
		sb.append(value);
		sb.append(" \"");
		value = bundle.getString("Build-Title");
		sb.append(value);
		sb.append("\", Version ");
		value = bundle.getString("Build-Version");
		sb.append(value);
		sb.append(", Build #");
		value = bundle.getString("Build-Number");
		sb.append(value);
		sb.append(" on ");
		value = bundle.getString("Build-Date");
		sb.append(value);
		return sb.toString();
	}
	
}
