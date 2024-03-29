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
package com.hulles.alixia.overmind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.hulles.alixia.api.dialog.DialogRequest;
import com.hulles.alixia.api.object.AlixiaClientObject;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.shared.SerialSememe;
import com.hulles.alixia.api.shared.SessionType;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.api.tools.AlixiaUtils;
import com.hulles.alixia.house.ClientDialogRequest;
import com.hulles.alixia.room.Room;
import com.hulles.alixia.room.document.RoomRequest;
import com.hulles.alixia.room.document.SememeAnalysis;
import com.hulles.alixia.ticket.ActionPackage;
import com.hulles.alixia.ticket.SememePackage;
import com.hulles.alixia.ticket.SentencePackage;
import com.hulles.alixia.ticket.Ticket;
import java.util.Iterator;

/**
 * Thimk handles decision points as we choose what to send on to the client. 
 * Its name comes from the "THIMK" signs that IBM used to (and perhaps still does) 
 * have displayed in their offices. Eventually I want Alixia to make the decisions 
 * here. Hello, TensorFlow....
 * 
 * @author hulles
 *
 */
final class Thimk {
	private final static Logger LOGGER = Logger.getLogger("AlixiaOvermind.Thimk");
    private final static Level LOGLEVEL = OvermindRoom.LOGLEVEL;
	private final static Random RANDOM = new Random();
	private final OvermindRoom overmind;
	
	Thimk(OvermindRoom overmind) {
	
		SharedUtils.checkNotNull(overmind);
		this.overmind = overmind;
	}
	
	/**
	 * Select an ActionPackage from among a list of available
	 * ActionPackages. Currently it is a random selection; eventually Alixia
	 * will decide which one is best.
	 * 
	 * @param sememe The sememe common to the ActionPackages
	 * @param pkgs The list of ActionPackages
     * @param ticket The ticket that came with the request
	 * @return The chosen ActionPackage
	 */
	static ActionPackage chooseAction(SerialSememe sememe, List<ActionPackage> pkgs, Ticket ticket) {
		ActionPackage pkg;
		int pkgIx;
        DialogRequest dialogRequest;
        
		SharedUtils.checkNotNull(sememe);
		SharedUtils.checkNotNull(pkgs);
        SharedUtils.checkNotNull(ticket);
		LOGGER.log(LOGLEVEL, "Thimk: chooseAction");
        dialogRequest = ticket.getJournal().getClientRequest().getDialogRequest();
        winnowPackages(sememe, pkgs, dialogRequest);
		if (pkgs.size() == 1) {
			// easy decision
			pkg = pkgs.get(0);
		} else if (sememe.is("sememe_analysis")) {
			// here, instead of returning an existing action package we're going to create a new one
			pkg = unifySememeAnalyses(pkgs);
		} else {
			// for now, just grab a random package; this is where Alixia will
			//    get to pick, eventually
			pkgIx = RANDOM.nextInt(pkgs.size());
			pkg = pkgs.get(pkgIx);
		}
		return pkg;
	}
	
    /**
     * This is a "smart" function where we reduce the set of available packages 
     * based on the DialogRequest we (hopefully) have. 
     * 
	 * @param sememe The sememe common to the ActionPackages
	 * @param pkgs The list of ActionPackages
     * @param request The DialogRequest we got with the client request
     * @return 
     */
    private static List<ActionPackage> winnowPackages(SerialSememe sememe, 
            List<ActionPackage> pkgs, DialogRequest request) {
        ActionPackage pkg;
        
        SharedUtils.checkNotNull(sememe);
        SharedUtils.checkNotNull(pkgs);
        SharedUtils.checkNotNull(request);
        if (pkgs.size() <= 1) {
            // nothing to choose
            return pkgs;
        }
        if ((request.getSessionType() == SessionType.TEXT) || request.isQuiet()) {
            for (Iterator<ActionPackage> iter = pkgs.iterator(); iter.hasNext(); ) {
                pkg = iter.next();
                if (pkg.isMultiMedia()) {
                    iter.remove();
                    if (pkgs.size() <= 1) {
                        break;
                    }
                }
            }
        }
        return pkgs;
    }
    
	/**
	 * This method takes all the sememe analyses from the various rooms, assuming there are
	 * some, and ends up with a new action package with a new SerialSememe Analysis that contains 
	 * at most one sememe package per sentence. We do this because it seems to make 
	 * conversational sense; we can change it later if seems like we could do better.
	 * 
	 * @param actionPackages
	 * @return
	 */
	private static ActionPackage unifySememeAnalyses(List<ActionPackage> actionPackages) {
		ListMultimap<String, SememePackage> sentenceSememePackages;
		SememeAnalysis analysis;
		List<SememePackage> sememePackages;
		List<SememePackage> unifiedSememePackages;
		SememeAnalysis unifiedAnalysis;
		ActionPackage newActionPackage;
		SememePackage newSememePkg;
		SentencePackage sentencePackage;
		
		SharedUtils.checkNotNull(actionPackages);
		unifiedSememePackages = new ArrayList<>();
		sentenceSememePackages = MultimapBuilder.hashKeys().arrayListValues().build();
		for (ActionPackage pkg : actionPackages) {
			analysis = (SememeAnalysis) pkg.getActionObject();
			sememePackages = analysis.getSememePackages();
			for (SememePackage sememePkg : sememePackages) {
				sentencePackage = sememePkg.getSentencePackage();
				if (sentencePackage == null) {
					AlixiaUtils.error("Thimk: null sentence package for sememe " + sememePkg.getName(),
							"This should not be the case at this point in the analysis.");
				} else {
					sentenceSememePackages.put(sentencePackage.getSentencePackageID(), sememePkg);
				}
			}
		}
		for (String sentencePackageID : sentenceSememePackages.keySet()) {
			LOGGER.log(LOGLEVEL, "Thimk unifySememeAnalyses: sentencePackageID = {0}", 
                    sentencePackageID);
			sememePackages = sentenceSememePackages.get(sentencePackageID);
			LOGGER.log(LOGLEVEL, "Thimk unifySememeAnalyses: sememe packages count = {0}", 
                    sememePackages.size());
			for (SememePackage sp : sememePackages) {
				LOGGER.log(LOGLEVEL, "Thimk unifySememeAnalyses: sememe package {0} score is {1}", 
                        new Object[]{sp.getName(), sp.getConfidence()});
			}
			if (sememePackages.isEmpty()) {
				continue;
			}
			if (sememePackages.size() == 1) {
				unifiedSememePackages.add(sememePackages.get(0));
				continue;
			}
			// fine, we have multiple sememe packages for a sentence; we need to pick one
			Collections.sort(sememePackages, new Comparator<SememePackage>() {
				@Override
				public int compare(SememePackage o1, SememePackage o2) {
					return o2.getConfidence().compareTo(o1.getConfidence());
				}
			});
			unifiedSememePackages.add(sememePackages.get(0));
		}
		unifiedAnalysis = new SememeAnalysis();
		unifiedAnalysis.setSememePackages(unifiedSememePackages);
		newSememePkg = SememePackage.getDefaultPackage("sememe_analysis");
		if (!newSememePkg.isValid()) {
			throw new AlixiaException("Thimk: created invalid sememe package");
		}
		newActionPackage = new ActionPackage(newSememePkg);
		newActionPackage.setActionObject(unifiedAnalysis);
		// whew
		return newActionPackage;
	}
	
	/**
	 * Decide what to do after receiving a client request. Currently there isn't really
	 * a decision to make, we just throw it at the NLP analyzers (just Charlie, for now).
	 * 
	 * @param ticket
	 * @param request
	 */
	void decideClientRequestNextAction(Ticket ticket, ClientDialogRequest request) {
		RoomRequest newRequest;
		String msg;
		AlixiaClientObject obj;
		List<SememePackage> sememePackages;
		DialogRequest dialogRequest;
		
		SharedUtils.checkNotNull(ticket);
		SharedUtils.checkNotNull(request);
		LOGGER.log(LOGLEVEL, "Thimk: decideClientRequestNextAction entry");
		dialogRequest = request.getDialogRequest();
		msg = dialogRequest.getRequestMessage();
		obj = dialogRequest.getClientObject();
		if ((msg == null || msg.isEmpty()) && obj == null) {
			// nothing to do
			LOGGER.log(LOGLEVEL, "Thimk: nothing to do");
			sememePackages = SememePackage.getSingletonDefault("nothing_to_do");
			overmind.terminatePipeline(ticket, sememePackages);
			return;
		}
		
		// okay, we need to figure out what's wanted, so first we start with awesome Charlie
		//    for some NLP action
		LOGGER.log(LOGLEVEL, "Thimk: decideClientRequestNextAction, msg = {0}", msg);
		newRequest = new RoomRequest(ticket);
		newRequest.setFromRoom(Room.OVERMIND);
		sememePackages = SememePackage.getSingletonDefault("nlp_analysis");
		newRequest.setSememePackages(sememePackages);
		newRequest.setMessage(msg);
		newRequest.setRoomObject(null);
		overmind.sendRoomRequest(newRequest);
	}
	
	/**
	 * Decide what to do after receiving the NLP analysis.
	 * 
	 * @param ticket The associated ticket (the sentence packages are now in the TicketJournal
	 */
	void decideNlpAnalysisNextAction(Ticket ticket) {
		RoomRequest newRequest;
		List<SememePackage> sememePackages;

		SharedUtils.checkNotNull(ticket);
		LOGGER.log(LOGLEVEL, "Thimk: decideNlpAnalysisNextAction");
		
		// Next, we ask for a sememe analysis, to match up sememes with our input sentences
		newRequest = new RoomRequest(ticket);
		// we proxy the From room back to Overmind (we're not a full-fledged Room ourselves (yet))
		newRequest.setFromRoom(Room.OVERMIND);
		sememePackages = SememePackage.getSingletonDefault("sememe_analysis");
		newRequest.setSememePackages(sememePackages);
		newRequest.setMessage("Asking for sememe analysis");
		overmind.sendRoomRequest(newRequest);		
	}
	
	/**
	 * Decide what to do after receiving the sememe analysis.
	 * This is currently the end of the pipeline, so we proceed in a calm and 
	 * orderly fashion to the exit.
	 * 
	 * @param ticket The associated ticket
	 * @param sememePackages The accumulated sememe packages
	 */
	void decideSememeAnalysisNextAction(Ticket ticket, List<SememePackage> sememePackages) {
		
		SharedUtils.checkNotNull(ticket);
		LOGGER.log(LOGLEVEL, "Thimk: decideSememeAnalysisNextAction");
		overmind.terminatePipeline(ticket, sememePackages);
	}

}
