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
package com.hulles.alixia.webx.shared;

import java.io.Serializable;

import com.hulles.alixia.prong.shared.SerialProng;


/**
 * This class is for serialized system info that the client needs from the server
 * at (or around) startup
 * 
 * @author hulles
 *
 */
final public class SerialSystemInfo implements Serializable {
	private static final long serialVersionUID = 3682192200361590924L;
	private String version;
	private SerialProng prongValue;
	
	public SerialSystemInfo() {
		// need no-arg constructor
	}

	public String getVersion() {
		
		return version;
	}

	public void setVersion(String version) {
		
		SharedUtils.checkNotNull(version);
		this.version = version;
	}

	public SerialProng getProngValue() {
		
		return prongValue;
	}

	public void setProngValue(SerialProng prongValue) {
		
		SharedUtils.checkNotNull(prongValue);
		this.prongValue = prongValue;
	}	
	
}
