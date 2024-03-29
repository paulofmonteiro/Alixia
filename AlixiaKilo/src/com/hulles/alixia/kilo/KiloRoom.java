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
package com.hulles.alixia.kilo;

import com.hulles.alixia.api.shared.AlixiaException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.shared.ApplicationKeys;
import com.hulles.alixia.api.shared.ApplicationKeys.ApplicationKey;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.cayenne.OwmCity;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.RoomActionObject;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Kilo Room is our weather room. I'm currently using Open Weather Room for the data, and I'm
 * mostly happy with it, but I may add some other services to compare. (Weather Underground?) 
 * 
 * @author hulles
 *
 */
public final class KiloRoom extends UrRoom {
	private final ApplicationKeys appKeys;

	public KiloRoom() {
		super();
		
		appKeys = ApplicationKeys.getInstance();
	}

	@Override
	public Room getThisRoom() {

		return Room.KILO;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new AlixiaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	protected void roomStartup() {
	}

	@Override
	protected void roomShutdown() {
	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		switch (sememePkg.getName()) {
			case "weather_forecast":
				return createForecastActionPackage(sememePkg, request);
			case "current_weather":
				return createWeatherActionPackage(sememePkg, request);
			case "this_location":
				return createLocationActionPackage(sememePkg, request);
			case "current_time":
				return createTimeActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private static ActionPackage createTimeActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		KiloTimeAction action;
		LocalDateTime now;
		KiloLocationAction locationAction;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		locationAction = KiloLocation.getLocation();
		action = new KiloTimeAction();
		action.setLocation(locationAction.getCity());
		now = LocalDateTime.now();
		action.setLocalDateTime(now);
		pkg = new ActionPackage(sememePkg);
		pkg.setActionObject(action);
		return pkg;
	}
	
	private ActionPackage createForecastActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		RoomActionObject action;
		String idStr;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		idStr = appKeys.getKey(ApplicationKey.OWMCITY);
		action = KiloWeather.getForecastWeather(Integer.parseInt(idStr));
		pkg = new ActionPackage(sememePkg);
		pkg.setActionObject(action);
		return pkg;
	}

	private ActionPackage createWeatherActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		RoomActionObject action;
		String idStr;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		idStr = appKeys.getKey(ApplicationKey.OWMCITY);
		action = KiloWeather.getCurrentWeather(Integer.parseInt(idStr));
		pkg = new ActionPackage(sememePkg);
		pkg.setActionObject(action);
		return pkg;
	}

	private static ActionPackage createLocationActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		RoomActionObject action;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		action = KiloLocation.getLocation();
		pkg = new ActionPackage(sememePkg);
		pkg.setActionObject(action);
		return pkg;
	}
	
	@SuppressWarnings("unused")
	private static KiloLocationAction findLocation(String sememeObject) {
		List<OwmCity> cities;
		KiloLocationAction action;
		OwmCity match;
		
		SharedUtils.checkNotNull(sememeObject);
		cities = OwmCity.getOwmCities(sememeObject);
		if (cities.isEmpty()) {
			// whoever created the initial sememe package should have vetted
			//    the city already, from NER e.g.
			AlixiaUtils.error("KiloRoom:findLocation: location not found");
			return null;
		}
		// TODO ask client to narrow down list of cities
		//  For now we'll just take the first one
		match = cities.get(0);
		action = new KiloLocationAction();
		action.setCity(match.getName());
		action.setCountry(match.getCountry().getName());
		action.setLatitude(match.getLatitude());
		action.setLongitude(match.getLongitude());
		action.setOwmCityID(match.getOwmId());
		return action;
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("weather_forecast"));
		sememes.add(SerialSememe.find("current_weather"));
		sememes.add(SerialSememe.find("current_weather_location"));
		sememes.add(SerialSememe.find("this_location"));
		sememes.add(SerialSememe.find("current_time"));
		sememes.add(SerialSememe.find("current_time_location"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
