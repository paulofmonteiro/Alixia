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
package com.hulles.a1icia.hotel;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.EventBus;
import com.hulles.a1icia.api.A1iciaConstants;
import com.hulles.a1icia.api.remote.A1icianID;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.NamedTimer;
import com.hulles.a1icia.cayenne.Spark;
import com.hulles.a1icia.cayenne.Task;
import com.hulles.a1icia.hotel.task.LoadTasks;
import com.hulles.a1icia.house.ClientDialogResponse;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.MessageAction;
import com.hulles.a1icia.room.document.RoomActionObject;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SparkObjectType;
import com.hulles.a1icia.ticket.SparkPackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.tools.A1iciaUtils;

/**
 * Hotel Room has risen from the ashes and become our calendar room.
 * 
 * @author hulles
 *
 */
public final class HotelRoom extends UrRoom {
	private final static Logger LOGGER = Logger.getLogger("A1iciaHotel.A1iciaHotel");
	private final static Level LOGLEVEL = A1iciaConstants.getA1iciaLogLevel();
	private TimerHandler timerHandler;
	
	public HotelRoom(EventBus bus) {
		super(bus);
		
	}

	@Override
	public Room getThisRoom() {

		return Room.HOTEL;
	}

	/**
	 * While this looks like a lot of nonsense, and on one level that's true of course, it also
	 * tests the complex machinery that generates responses to requests. That's my story and I'm
	 * sticking to it.
	 * 
	 */
	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		List<ActionPackage> pkgs;
		RoomActionObject obj;
		MessageAction msgAction;
		Ticket ticket;
		ClientObjectWrapper cow;
		
		A1iciaUtils.checkNotNull(request);
		A1iciaUtils.checkNotNull(responses);
		// note that here we're ignoring the fact that we might get more than one response, 
		// particularly for the media request -- FIXME
		for (RoomResponse rr : responses) {
			if (!rr.hasNoResponse()) {
				// see if we can learn anything....
				pkgs = rr.getActionPackages();
				for (ActionPackage pkg : pkgs) {
					obj = pkg.getActionObject();
					if (obj instanceof MessageAction) {
						msgAction = (MessageAction) obj;
							LOGGER.log(LOGLEVEL, "We got some learning => " + msgAction.getMessage() +
									" : " + msgAction.getExplanation());
					} else if (obj instanceof ClientObjectWrapper) {
						cow = (ClientObjectWrapper) obj;
						timerHandler.setMediaFile(cow);
					}
				}
			}
		}
		ticket = request.getTicket();
		ticket.close();
	}

	@Override
	protected void roomStartup() {
		List<Task> tasks;
		
		timerHandler = new TimerHandler(this);
		tasks = Task.getAllTasks();
		if (tasks.isEmpty()) {
			LoadTasks.loadTasks();
		}
	}

	@Override
	protected void roomShutdown() {
		
		timerHandler.close();
	}
	
	@Override
	protected ActionPackage createActionPackage(SparkPackage sparkPkg, RoomRequest request) {

		switch (sparkPkg.getName()) {
			case "named_timer":
			case "duration_timer":
				return createTimerActionPackage(sparkPkg, request);
			default:
				throw new A1iciaException("Received unknown spark in " + getThisRoom());
		}
	}

	private ActionPackage createTimerActionPackage(SparkPackage sparkPkg, RoomRequest request) {
		String result = null;
		MessageAction response;
		ActionPackage pkg;
		NamedTimer dbTimer;
		SparkPackage namedTimerPkg;
		String timerName;
		A1icianID a1icianID;
		Ticket ticket;
		
		A1iciaUtils.checkNotNull(sparkPkg);
		A1iciaUtils.checkNotNull(request);
		if (sparkPkg.is("duration_timer")) {
			A1iciaUtils.error("HotelRoom: got duration timer spark, but we can't handle that yet");
			return null;
		}
		// named_timer
		namedTimerPkg = sparkPkg;
		if (namedTimerPkg.getSparkObjectType() != SparkObjectType.TIMERNAME) {
			A1iciaUtils.error("HotelRoom: got named timer spark, but spark object type is not timer name");
			return null;
		}
		timerName = namedTimerPkg.getSparkObject();
		dbTimer = NamedTimer.findNamedTimer(timerName);
		if (dbTimer == null) {
			A1iciaUtils.error("HotelRoom: can't find timer named " + timerName + " in database");
			return null;
		}
		ticket = request.getTicket();
		a1icianID = ticket.getFromA1icianID();
		timerHandler.setNewTimer(a1icianID, timerName, dbTimer.getDurationMs());
		pkg = new ActionPackage(sparkPkg);
		response = new MessageAction();
		result = timerName + " timer is set.";
		response.setMessage(result);
		pkg.setActionObject(response);
		return pkg;
	}
	
	public void postRequest(ClientDialogResponse response) {
		Ticket ticket;
		RoomRequest roomRequest;

		A1iciaUtils.checkNotNull(response);
		ticket = Ticket.createNewTicket(getHall(), getThisRoom());
		ticket.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		roomRequest = new RoomRequest(ticket);
		roomRequest.setFromRoom(getThisRoom());
		roomRequest.setSparkPackages(SparkPackage.getSingletonDefault("indie_response"));
		roomRequest.setMessage("Indie client response");
		roomRequest.setRoomObject(response);
		sendRoomRequest(roomRequest);
	}
	
	public void getMedia(Long timerID, String notificationTitle) {
		RoomRequest mediaRequest;
		Ticket ticket;
		SparkPackage sparkPkg;
		
		A1iciaUtils.checkNotNull(timerID);
		A1iciaUtils.checkNotNull(notificationTitle);
		// now we pop off a request to Mike or whoever to get us some media bytes
		ticket = Ticket.createNewTicket(getHall(), getThisRoom());
		ticket.setFromA1icianID(A1iciaConstants.getA1iciaA1icianID());
		mediaRequest = new RoomRequest(ticket);
		mediaRequest.setFromRoom(getThisRoom());
		sparkPkg = SparkPackage.getDefaultPackage("notification_medium");
		sparkPkg.setSparkObjectType(SparkObjectType.AUDIOTITLE);
		sparkPkg.setSparkObject(notificationTitle);
		mediaRequest.setSparkPackages(Collections.singletonList(sparkPkg));
		mediaRequest.setMessage(timerID.toString()); // sort of a kluge, but...
//		mediaRequest.setMindObject(response);
		sendRoomRequest(mediaRequest);
	}
	
	@Override
	protected Set<Spark> loadSparks() {
		Set<Spark> sparks;
		
		sparks = new HashSet<>();
		sparks.add(Spark.find("named_timer"));
		sparks.add(Spark.find("duration_timer"));
		return sparks;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}