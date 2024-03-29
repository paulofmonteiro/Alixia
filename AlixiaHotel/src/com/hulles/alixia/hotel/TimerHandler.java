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
package com.hulles.alixia.hotel;

import java.io.Closeable;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.dialog.DialogResponse;
import com.hulles.alixia.api.jebus.JebusBible;
import com.hulles.alixia.api.jebus.JebusBible.JebusKey;
import com.hulles.alixia.api.jebus.JebusHub;
import com.hulles.alixia.api.jebus.JebusPool;
import com.hulles.alixia.api.object.MediaObject;
import com.hulles.alixia.api.remote.AlixianID;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.house.ClientDialogResponse;
import com.hulles.alixia.media.Language;
import com.hulles.alixia.room.document.ClientObjectWrapper;

import redis.clients.jedis.Jedis;

final class TimerHandler implements Closeable {
	private final static int LED_ON_MINUTES = 2;
	final Queue<HotelTimer> timerQueue;
	private final ScheduledExecutorService executor;
	final HotelRoom hotelRoom;
	
	TimerHandler(HotelRoom room) {
		
		SharedUtils.checkNotNull(room);
		hotelRoom = room;
		timerQueue = new ConcurrentLinkedQueue<>();
		executor = Executors.newScheduledThreadPool(1);
		addDelayedShutdownHook(executor);
		startTimerService();
	}
	
/*	public void setNewTimer(SerialUUID<SerialPerson> personUUID, String timerName, Integer minutes) {
		HotelTimer timer;
		Long millis;
		
		SharedUtils.checkNotNull(personUUID);
		SharedUtils.checkNotNull(timerName);
		SharedUtils.checkNotNull(minutes);
		timer = new HotelTimer();
		timer.setPersonUUID(personUUID);
		timer.setName(timerName);
		millis = System.currentTimeMillis();
		millis += minutes * 60 * 1000;
		timer.setExpires(millis);
		synchronized (timerList) {
			timerList.add(timer);
		}
	}
*/	
	public void setNewTimer(AlixianID alixianID, String timerName, Long millis) {
		HotelTimer timer;
		
		SharedUtils.checkNotNull(alixianID);
		SharedUtils.checkNotNull(timerName);
		SharedUtils.checkNotNull(millis);
		timer = new HotelTimer();
//		timer.setPersonUUID(personUUID);
		timer.setAlixianID(alixianID);
		timer.setName(timerName);
		millis += System.currentTimeMillis();
		timer.setExpires(millis);
		timerQueue.add(timer);
		hotelRoom.getMedia(timer.getTimerID(), "bootsandsaddles.wav");
	}
	
	public void setMediaFile(ClientObjectWrapper mediaObject) {
		Long timerID;
		
		SharedUtils.checkNotNull(mediaObject);
		timerID = Long.parseLong(mediaObject.getMessage());
		for (HotelTimer timer : timerQueue) {
			if (timer.getTimerID().equals(timerID)) {
				timer.setMediaObject(mediaObject);
				break;
			}
		}
	}
	
	private void startTimerService() {
		
		final Runnable checker = new Runnable() {
			@Override
			public void run() {
				Long now;
				HotelTimer timer;
				
				now = System.currentTimeMillis();
				for (Iterator<HotelTimer> iter = timerQueue.iterator(); iter.hasNext(); ) {
					timer = iter.next();
					if (timer.getExpires() < now) {
						pushNotification(timer);
							iter.remove();
					}
				}
			}
		};
		executor.scheduleAtFixedRate(checker, 60, 60, TimeUnit.SECONDS);
	}
	
	void pushNotification(HotelTimer timer) {
		ClientDialogResponse clientResponse;
		SerialSememe sememe;
		MediaObject mediaObject;
		DialogResponse response;
		
		SharedUtils.checkNotNull(timer);
		// first we send a wakeup call to the console
		clientResponse = new ClientDialogResponse();
		response = clientResponse.getDialogResponse();
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
//		response.setPersonUUID(timer.getPersonUUID());
		response.setToAlixianID(timer.getAlixianID());
		sememe = SerialSememe.find("wake_up_console");
		response.setResponseAction(sememe);
		hotelRoom.postRequest(clientResponse);
		
		// next we send a blinking LED, a sound clip and a message 
		clientResponse = new ClientDialogResponse();
		response = clientResponse.getDialogResponse();
		response.setLanguage(Language.AMERICAN_ENGLISH);
		response.setFromAlixianID(AlixiaConstants.getAlixiaAlixianID());
//		response.setPersonUUID(timer.getPersonUUID());
		response.setExplanation(timer.getName() + " has finished.");
		response.setToAlixianID(timer.getAlixianID());
		sememe = SerialSememe.find("blink_white_LED");
		response.setResponseAction(sememe);
		response.setMessage("Time's up!");
		// this should set the accompanying bugle call or whatever for the timer
		mediaObject = (MediaObject) timer.getMediaObject().getClientObject();
		response.setClientObject(mediaObject);
		hotelRoom.postRequest(clientResponse);
		
		turnOffLED(timer);
	}
	
	private void turnOffLED(final HotelTimer timer) {
		
		SharedUtils.checkNotNull(timer);
		final Runnable resetter = new Runnable() {
			@Override
			public void run() {
				ClientDialogResponse clientResponse;
				DialogResponse response;
				SerialSememe sememe;
				
				// we turn off the blinky LED if it wasn't manually cancelled already
				// TODO consider leaving it on to show that the event occurred
				clientResponse = new ClientDialogResponse();
				response = clientResponse.getDialogResponse();
				response.setLanguage(Language.AMERICAN_ENGLISH);
				response.setFromAlixianID(timer.getAlixianID());
//				response.setPersonUUID(timer.getPersonUUID());
				response.setToAlixianID(AlixiaConstants.getAlixiaAlixianID());
				sememe = SerialSememe.find("blink_white_LED");
				response.setResponseAction(sememe);
				hotelRoom.postRequest(clientResponse);
			}
		};
		executor.schedule(resetter, LED_ON_MINUTES, TimeUnit.MINUTES);
	}
	
	@Override
	public void close() {
		
		shutdownAndAwaitTermination(executor);
	}
	
	static void shutdownAndAwaitTermination(ExecutorService pool) {
		
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    AlixiaUtils.error("TimerHandler -- executor did not terminate");
                }
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
	
	private void addDelayedShutdownHook(final ScheduledExecutorService pool) {
		Runnable shutdownHook;
		Thread hook;
		
		shutdownHook = new ShutdownHook(pool);
		hook = new Thread(shutdownHook);
		Runtime.getRuntime().addShutdownHook(hook);
	}
	
	private class ShutdownHook implements Runnable {
		ScheduledExecutorService pool;
		
		ShutdownHook(ScheduledExecutorService pool) {
			this.pool = pool;
		}
		
	    @Override
		public void run() {
	    	
			shutdownAndAwaitTermination(pool);
	    }
	}
	
	private class HotelTimer {
		private final Long timerID;
		private String name;
//		private SerialUUID<SerialPerson> personUUID;
		private AlixianID alixianID;
		private Long expires;
		private ClientObjectWrapper mediaObject;
		
		HotelTimer() {
			
			timerID = getNewTimerID();
		}

		public Long getTimerID() {
			
			return timerID;
		}
		
		public String getName() {
			
			return name;
		}

		public void setName(String name) {
			
			SharedUtils.checkNotNull(name);
			this.name = name;
		}

		public AlixianID getAlixianID() {
			
			return alixianID;
		}

		public void setAlixianID(AlixianID alixianID) {
			
			SharedUtils.checkNotNull(alixianID);
			this.alixianID = alixianID;
		}

/*		public SerialUUID<SerialPerson> getPersonUUID() {
			
			return personUUID;
		}

		public void setPersonUUID(SerialUUID<SerialPerson> personUUID) {
			
			SharedUtils.checkNotNull(personUUID);
			this.personUUID = personUUID;
		}
*/
		public Long getExpires() {
			
			return expires;
		}

		public void setExpires(Long expires) {
			
			SharedUtils.checkNotNull(expires);
			this.expires = expires;
		}

		public ClientObjectWrapper getMediaObject() {
			
			return mediaObject;
		}

		public void setMediaObject(ClientObjectWrapper mediaObject) {
			
			SharedUtils.checkNotNull(mediaObject);
			this.mediaObject = mediaObject;
		}
	}
	
	static long getNewTimerID() {
		JebusPool jebusPool;
        String key;
        
		jebusPool = JebusHub.getJebusLocal();
		try (Jedis jebus = jebusPool.getResource()) {
			 key = JebusBible.getStringKey(JebusKey.ALIXIATIMERCOUNTERKEY, jebusPool);
			return jebus.incr(key);
		}		
	}
}
