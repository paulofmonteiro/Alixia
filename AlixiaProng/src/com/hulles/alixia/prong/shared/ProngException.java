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
package com.hulles.alixia.prong.shared;

public class ProngException extends Exception {
	private static final long serialVersionUID = 4066588339568803803L;
	private SerialProng badProng;

	public ProngException() {
		super();
		this.badProng = null;
	}
	
	public ProngException(SerialProng badProng) {
		
		super(badProng.getProngString());
		this.badProng = badProng;
	}

	public SerialProng getBadProng() {
		
		return badProng;
	}
	
}
