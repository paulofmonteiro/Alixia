/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
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
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.a1icia.graphviz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.hulles.a1icia.tools.A1iciaUtils;

public class GraphViz {
    private final static String DOT = "dot";
    private final static String TMP_PATH = "/tmp";
    private final String dotExec;
    private final String tmpPath;
    
    public GraphViz() {
    	
    	this.dotExec = DOT;
    	this.tmpPath = TMP_PATH;
    }
    public GraphViz(String dotPath, String tmpPath) {
    	
    	A1iciaUtils.checkNotNull(dotPath);
    	A1iciaUtils.checkNotNull(tmpPath);
        this.dotExec = dotPath;
        this.tmpPath= tmpPath;
    }

    public void writeGraphToImageFile(Graph graph, String type, String dpi, String outFile) {
    	byte[] graphBytes;
    	
    	graphBytes = getGraphBytes(graph, type, dpi);
    	writeGraphBytesToFile(graphBytes, outFile);
    }
    
    public byte[] getGraphBytes(Graph graph, String type, String dpi) {
        String dotSource;
        File dot = null;
        byte[] dotBytes = null;

        A1iciaUtils.checkNotNull(graph);
        A1iciaUtils.checkNotNull(type);
        A1iciaUtils.checkNotNull(dpi);
        dotSource = graph.genGraphDotString();
        dot = writeDotSourceToFile(dotSource);
        dotBytes = getImageArray(dot, type, "dot", dpi);
        dot.delete();
        return dotBytes;
    }

    public static void writeGraphBytesToFile(byte[] bytes, String outFile) {
    	Path path;

    	A1iciaUtils.checkNotNull(bytes);
    	A1iciaUtils.checkNotNull(outFile);
    	path = Paths.get(outFile);
    	try {
			Files.write(path, bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    private static File writeDotSourceToFile(String str) {
        File temp;
        
        A1iciaUtils.checkNotNull(str);
        try {
            temp = File.createTempFile("graph_", ".dot.tmp", new File(GraphViz.TMP_PATH));
            try (FileWriter writer = new FileWriter(temp)) {
                writer.write(str);
            }
        }
        catch (Exception e) {
            return null;
        }
        return temp;
    }

    private byte[] getImageArray(File dot, String type, String representationType, String dpi) {
        File imageFile = null;
        byte[] imageBytes = null;
        Runtime runtime;
        Process process;
        BufferedReader reader;
        String line;
        
        A1iciaUtils.checkNotNull(dot);
        A1iciaUtils.checkNotNull(type);
        A1iciaUtils.checkNotNull(representationType);
        A1iciaUtils.checkNotNull(dpi);
        try {
            imageFile = File.createTempFile("graphviz", type, new File(tmpPath));
            runtime = Runtime.getRuntime();
            String[] args = {dotExec, "-T"+type, "-K"+representationType, "-Gdpi="+dpi, dot.getAbsolutePath(), "-o", imageFile.getAbsolutePath()};
            process = runtime.exec(args);
            process.waitFor();
            
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = null;
            while ((line = reader.readLine()) != null) {
                line = reader.readLine();
                System.out.println(line);
            }
            
            try (FileInputStream finput = new FileInputStream(imageFile.getAbsolutePath())) {
	            imageBytes = new byte[finput.available()];
	            finput.read(imageBytes);
            }
            imageFile.delete();
        }
        catch (java.io.IOException ioe) {
            throw new GraphException(ioe.toString());
        }
        catch (InterruptedException ie) {
            throw new GraphException(ie.toString());
        } finally {
            try {
                if (imageFile != null) {
                    imageFile.delete();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return imageBytes;
    }

}
