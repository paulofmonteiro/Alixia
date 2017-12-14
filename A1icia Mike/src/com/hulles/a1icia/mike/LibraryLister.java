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
package com.hulles.a1icia.mike;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.hulles.a1icia.tools.A1iciaUtils;

public class LibraryLister extends SimpleFileVisitor<Path> {
//	private final static String WAVPATTERN = "*.wav";
	private final static String MEDIAPATTERN = "*.{wav,mp3,mp4,flv,mov,png,jpg,gif}";
	private final PathMatcher matcher;
//	private int numMatches = 0;
	private List<String> fileNames = null;
	
	LibraryLister(String pattern) {
		fileNames = new ArrayList<>();
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
	}

	public static List<String> listFiles(String logDir) {
		
		return listFiles(logDir, MEDIAPATTERN);
	}
	
	public static List<String> listFiles(String logDir, String pattern) {
        LibraryLister lister;
        Path logDirPath;
        
        A1iciaUtils.checkNotNull(logDir);
        lister = new LibraryLister(pattern);
        logDirPath = Paths.get(logDir);
        try {
			Files.walkFileTree(logDirPath, lister);
		} catch (IOException e) {
			System.err.println("IO Exception listing library files");
			e.printStackTrace();
		}
        lister.done();
        return lister.getFileNames();
	}
	
	// Compares the glob pattern against
	// the file or directory name.
	private void find(Path file) {
		Path filePath;
		
        A1iciaUtils.checkNotNull(file);
		filePath = file.getFileName();
		if (filePath != null && matcher.matches(filePath)) {
//			numMatches++;
//			System.out.println("Found file " + filePath);
			try {
				filePath = file.toRealPath();
			} catch (IOException e) {
				System.err.println("Can't create real path to file " + file.getFileName());
				e.printStackTrace();
				return;
			}
			fileNames.add(filePath.toString());
		}
	}

	private void done() {
//		System.out.println("LibraryLister matches: " + numMatches);
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

		A1iciaUtils.checkNotNull(file);
        A1iciaUtils.checkNotNull(attrs);
		find(file);
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {

		System.err.println(exc);
		return FileVisitResult.CONTINUE;
	}

	public List<String> getFileNames() {
		
		return fileNames;
	}
}



