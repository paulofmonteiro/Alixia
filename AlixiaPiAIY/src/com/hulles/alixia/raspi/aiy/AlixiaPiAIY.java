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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hulles.alixia.raspi.aiy;

import java.util.ResourceBundle;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.remote.Station;
import com.hulles.alixia.cli.AlixiaCLIConsole.ConsoleType;

/**
 *
 * @author hulles
 */
public final class AlixiaPiAIY  {
	private static final String BUNDLE_NAME = "com.hulles.alixia.raspi.aiy.Version";
	private final static String USAGE = "usage: java -jar AlixiaPiAIY.jar [--host=HOST] [--port=PORT] [--daemon] \n\twhere HOST = IP address and PORT = port number";
	private static PiConsole zero2;
	
	public static String getVersionString() {
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
   
    public static void main(String[] args) {
		String host;
		String portStr;
		Integer port;
		Station station;
		ConsoleType whichConsole;
		
		station = Station.getInstance();
		station.ensureStationExists();
		host = station.getCentralHost();
		port = station.getCentralPort();
		whichConsole = ConsoleType.DEFAULT;
		for (String arg : args) {
			if (arg.startsWith("--host=")) {
				host = arg.substring(7);
			} else if (arg.startsWith("--port=")) {
				portStr = arg.substring(7);
				port = Integer.parseInt(portStr);
			} else if (arg.equals("--daemon")) {
				whichConsole = ConsoleType.DAEMON;
			} else {
				System.out.println(USAGE);
				System.exit(1);
			}
		}
		System.out.println(getVersionString());
		System.out.println(AlixiaConstants.getAlixiasWelcome());
		System.out.println();
		try (HardwareLayer hardwareLayer = new HardwareLayer()) {
			zero2 = new PiConsole(host, port, whichConsole, hardwareLayer);
			hardwareLayer.setConsole(zero2);
			hardwareLayer.setLED("blink_blue_LED"); // for now
			zero2.startAsync();
			zero2.awaitTerminated();
		}
    }

}
