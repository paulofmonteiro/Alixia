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
package com.hulles.a1icia.papa;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.a1icia.api.object.A1iciaClientObject.ClientObjectType;
import com.hulles.a1icia.api.object.MediaObject;
import com.hulles.a1icia.api.shared.SerialSememe;
import com.hulles.a1icia.api.shared.SharedUtils;
import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.crypto.PurdahKeys;
import com.hulles.a1icia.crypto.PurdahKeys.PurdahKey;
import com.hulles.a1icia.jebus.JebusHub;
import com.hulles.a1icia.media.MediaFormat;
import com.hulles.a1icia.media.MediaUtils;
import com.hulles.a1icia.room.Room;
import com.hulles.a1icia.room.UrRoom;
import com.hulles.a1icia.room.document.ClientObjectWrapper;
import com.hulles.a1icia.room.document.RoomAnnouncement;
import com.hulles.a1icia.room.document.RoomRequest;
import com.hulles.a1icia.room.document.RoomResponse;
import com.hulles.a1icia.room.document.SememeAnalysis;
import com.hulles.a1icia.ticket.ActionPackage;
import com.hulles.a1icia.ticket.SememePackage;
import com.hulles.a1icia.ticket.SentencePackage;
import com.hulles.a1icia.ticket.Ticket;
import com.hulles.a1icia.ticket.TicketJournal;
import com.hulles.a1icia.tools.A1iciaUtils;
import com.hulles.a1icia.tools.ExternalAperture;

/**
 * Papa|Room is all about Wolfram|Alpha. 
 * 
 * @author hulles
 *
 */
public final class PapaRoom extends UrRoom {
	private final static int MAXHEADROOM = JebusHub.getMaxHardOutputBufferLimit();
//	final static Logger LOGGER = Logger.getLogger("A1iciaPapa.PapaRoom");
//	private static final Level LOGLEVEL = Level.INFO;
	private String wolframKey;
	@SuppressWarnings("unused")
	private String wolframRemoteKey;
	
	public PapaRoom() {
		super();

	}

	@Override
	protected ActionPackage createActionPackage(SememePackage sememePkg, RoomRequest request) {

		switch (sememePkg.getName()) {
			case "sememe_analysis":
				return createAnalysisActionPackage(sememePkg, request);
			case "lookup_fact":
			case "define_word_or_phrase":
			case "who_is":
				return createLookupActionPackage(sememePkg, request);
			default:
				throw new A1iciaException("Received unknown sememe in " + getThisRoom());
		}
	}
	
	/**
 	 * We query W|A to see if any of our sentences qualify for later data lookup actions. We
 	 * send each sentence to their FastQuery service and they tell us if the sentence can be
 	 * treated as a legitimate data query. Usually under 10ms, they say.
 	 *   
	 * @param sememePkg
	 * @param request
	 * @return A SememeAnalysis package
	 */
	@SuppressWarnings("unused")
	private static ActionPackage createAnalysisActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage actionPkg;
		SememeAnalysis analysis;
		Ticket ticket;
		TicketJournal journal;
		List<SememePackage> sememePackages;
		List<SentencePackage> sentencePackages;
		SememePackage newSememePkg;
		String responseXML;
		String query;
		String responseMagic;
		String encodedQuery;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		ticket = request.getTicket();
		journal = ticket.getJournal();
		actionPkg = new ActionPackage(sememePkg);
		sememePackages = new ArrayList<>();
		sentencePackages = journal.getSentencePackages();
		analysis = new SememeAnalysis();
		for (SentencePackage sentencePackage : sentencePackages) {
			query = sentencePackage.getStrippedSentence(); // use (mostly) raw input for this
			encodedQuery = encodeQuery(query);
			
//			responseXML = ExternalAperture.getWolframValidateQuery(encodedQuery, wolframKey);
//			responseXML = ExternalAperture.getWolframQuery(encodedQuery, wolframKey);
			
//			System.out.println("RESPONSE XML:\n");
//			System.out.println(responseXML);
//			System.out.println();
			responseMagic = "Parse XML and get results dammit";
			if (responseMagic.isEmpty()) {
				newSememePkg = SememePackage.getDefaultPackage("lookup_fact");
				newSememePkg.setSentencePackage(sentencePackage);
				newSememePkg.setConfidence(98);
				newSememePkg.setSememeObject(query);
				if (!newSememePkg.isValid()) {
					throw new A1iciaException("PapaRoom: created invalid sememe package");
				}
				sememePackages.add(newSememePkg);
			}
		}
		analysis.setSememePackages(sememePackages);
		actionPkg.setActionObject(analysis);
		return actionPkg;
	}

	private ActionPackage createLookupActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		ClientObjectWrapper action;
		MediaObject mediaObject;
		String lookupString;
		String message = null;
		String encodedQuery;
		String responseText = null;
		BufferedImage responseImage = null;
		byte[] imageBytes = null;
		SentencePackage sentencePkg;
		byte[][] imageArrays;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		sentencePkg = sememePkg.getSentencePackage();
		if (sentencePkg == null) {
			A1iciaUtils.error("PapaRoom: null sentence package, should not be true",
					"SerialSememe is "  + sememePkg.getName());
			return null;
		}
		// we look up the (fixed) original sentence instead of the sememeObject because
		//    we are a manly heroic room, unlike some others I could name (*golf*).
		lookupString = sentencePkg.getStrippedSentence();
		if (lookupString == null) {
			A1iciaUtils.error("PapaRoom: sentence package has null original sentence");
			return null;
		}
		encodedQuery = encodeQuery(lookupString);
		// FIXME We should send the first result ("spoken" query) on ahead of the send result;
		//  the second query takes much longer
		responseText = ExternalAperture.getWolframSpokenQuery(encodedQuery, wolframKey);
		responseImage = ExternalAperture.getWolframSimpleQuery(encodedQuery, wolframKey);
		if (responseImage == null) {
			A1iciaUtils.error("PapaRoom: Wolfram|Alpha returned null image");
			return null;
		}
		try {
			imageBytes = MediaUtils.imageToByteArray(responseImage);
		} catch (IOException e) {
			throw new A1iciaException("PapaRoom: can't convert image", e);
		}
		message = responseText;
		if (imageBytes.length > MAXHEADROOM) {
			A1iciaUtils.error("PapaRoom: image file too big for Redis");
			return null;
		}
		imageArrays = new byte[][] {imageBytes};
		mediaObject = new MediaObject();
		mediaObject.setMediaBytes(imageArrays);
		// W|A docs say that the image may occasionally be a JPEG; if it's a problem
		//    we'll fix it
		mediaObject.setClientObjectType(ClientObjectType.IMAGEBYTES);
		mediaObject.setMediaFormat(MediaFormat.GIF);
		if (!mediaObject.isValid()) {
			A1iciaUtils.error("PapaRoom: invalid media object");
			return null;
		}
		action = new ClientObjectWrapper(mediaObject);
		action.setMessage(message);
		action.setExplanation("This information is from Wolfram|Alpha via Papa.");
		pkg.setActionObject(action);
		return pkg;
	}
	
	static String encodeQuery(String query) {
		String encoded;
		
		try {
			encoded = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new A1iciaException("Error encoding query in PapaRoom", e);
		}
		return encoded;
	}
	
	@Override
	public Room getThisRoom() {

		return Room.PAPA;
	}

	@Override
	public void processRoomResponses(RoomRequest request, List<RoomResponse> responses) {
		throw new A1iciaException("Response not implemented in " + 
				getThisRoom().getDisplayName());
	}

	@Override
	public void roomStartup() {
		PurdahKeys pKeys;
		
		// TODO here November has to load PurdahKeys before this executes. We're doing
		//   that in November's contructor, but just note that the comes-first relationship
		//   is there
		pKeys = PurdahKeys.getInstance();
		wolframKey = pKeys.getPurdahKey(PurdahKey.WOLFRAMALPHAKEY);
		wolframRemoteKey = pKeys.getPurdahKey(PurdahKey.WOLFRAMALPHAREMOTEKEY);
	}

	@Override
	protected void roomShutdown() {
		
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("sememe_analysis"));
		sememes.add(SerialSememe.find("define_word_or_phrase"));
		sememes.add(SerialSememe.find("who_is"));
		sememes.add(SerialSememe.find("lookup_fact"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
