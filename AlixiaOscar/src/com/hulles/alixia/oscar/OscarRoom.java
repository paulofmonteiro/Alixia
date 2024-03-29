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
package com.hulles.alixia.oscar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hulles.alixia.api.object.ChangeLanguageObject;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.media.Language;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.UrRoom;
import com.hulles.alixia.room.document.ClientObjectWrapper;
import com.hulles.alixia.room.document.MessageAction;
import com.hulles.alixia.room.document.RoomAnnouncement;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.RoomResponse;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;

/**
 * Oscar Room handles what are essentially constant values, like Alixia's name. Which is Alixia.
 *
 * @author hulles
 *
 */
public final class OscarRoom extends UrRoom {
	private final OscarResponder chooser;
	
	public OscarRoom() {
		super();
		
		chooser = new OscarResponder();
	}
	
	@Override
	public Room getThisRoom() {

		return Room.OSCAR;
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

		switch (sememePkg.getName()) {
			case "change_language":
				return createLanguageActionPackage(sememePkg, request);
			case "help":
			case "what_is_your_age":
			case "what_is_your_name":
			case "dislike_pickles":
			case "when_were_you_born":
			case "what_is_pi":
			case "like_waffles":
			case "listen_to_her_heart":
			case "rectum":
			case "personal_assistant":
			case "amanuensis":
			case "pronounce_hulles":
			case "like_gophers":
			case "not_dave":
            case "i_fink_u_freeky":
				return createConstantsActionPackage(sememePkg, request);
			default:
				throw new AlixiaException("Received unknown sememe in " + getThisRoom());
		}
	}

	private ActionPackage createConstantsActionPackage(SememePackage sememePkg, RoomRequest request) {
		String message;
		ActionPackage pkg;
		MessageAction action;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		pkg = new ActionPackage(sememePkg);
		action = new MessageAction();
		message = chooser.respondTo(sememePkg);
		action.setMessage(message);
		pkg.setActionObject(action);
		return pkg;
	}

	private static ActionPackage createLanguageActionPackage(SememePackage sememePkg, RoomRequest request) {
		ActionPackage pkg;
		Language language;
		String langStr;
		ChangeLanguageObject changeLang;
		ClientObjectWrapper clientChange;
		
		SharedUtils.checkNotNull(sememePkg);
		SharedUtils.checkNotNull(request);
		langStr = sememePkg.getSememeObject();
		language = Language.valueOf(langStr);
		changeLang = new ChangeLanguageObject();
		changeLang.setNewLanguage(language);
		clientChange = new ClientObjectWrapper(changeLang);
		clientChange.setMessage("Changed language to " + language.getDisplayName());
		pkg = new ActionPackage(sememePkg);
		pkg.setActionObject(clientChange);
		return pkg;
	}
	
	@Override
	protected Set<SerialSememe> loadSememes() {
		Set<SerialSememe> sememes;
		
		sememes = new HashSet<>();
		sememes.add(SerialSememe.find("help"));
		sememes.add(SerialSememe.find("what_is_your_name"));
		sememes.add(SerialSememe.find("dislike_pickles"));
		sememes.add(SerialSememe.find("what_is_your_age"));
		sememes.add(SerialSememe.find("when_were_you_born"));
		sememes.add(SerialSememe.find("what_is_pi"));
		sememes.add(SerialSememe.find("like_waffles"));
		sememes.add(SerialSememe.find("listen_to_her_heart"));
		sememes.add(SerialSememe.find("rectum"));
		sememes.add(SerialSememe.find("personal_assistant"));
		sememes.add(SerialSememe.find("amanuensis"));
		sememes.add(SerialSememe.find("pronounce_hulles"));
		sememes.add(SerialSememe.find("change_language"));
		sememes.add(SerialSememe.find("like_gophers"));
		sememes.add(SerialSememe.find("not_dave"));
        sememes.add(SerialSememe.find("i_fink_u_freeky"));
		return sememes;
	}

	@Override
	protected void processRoomAnnouncement(RoomAnnouncement announcement) {
	}

}
