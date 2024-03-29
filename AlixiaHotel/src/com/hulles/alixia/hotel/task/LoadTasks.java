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
package com.hulles.alixia.hotel.task;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.hulles.alixia.cayenne.AlixiaApplication;
import com.hulles.alixia.cayenne.Person;
import com.hulles.alixia.cayenne.Task;
import com.hulles.alixia.cayenne.TaskPriority;
import com.hulles.alixia.cayenne.TaskStatus;
import com.hulles.alixia.cayenne.TaskType;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VTodo;
import biweekly.property.Completed;
import biweekly.property.Created;
import biweekly.property.DateDue;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Uid;
import com.hulles.alixia.api.AlixiaConstants;
import com.hulles.alixia.api.shared.AlixiaException;
import com.hulles.alixia.api.tools.AlixiaUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadTasks {
	private final static Logger LOGGER = Logger.getLogger("AlixiaHotel.LoadTasks");
	private final static Level LOGLEVEL = AlixiaConstants.getAlixiaLogLevel();
	
	public static void loadTasks() {
		String fileName;
		ICalendar ical;
		List<VTodo> todos;
		Task task;
		Completed completed;
		Date date;
		LocalDateTime ldt;
		Created created;
		DateDue dateDue;
		TaskStatus taskStatus;
		Person person;
		TaskPriority taskPriority;
		TaskType taskType;
		Summary summary;
        Status status;
        String statusValue;
        Uid uid;
        List<VEvent> events;
        String eventSummary;
        
		person = Person.findPerson("hulles");
		taskPriority = TaskPriority.findTaskPriority(2);
		taskType = TaskType.findTaskType(1);
		fileName = "/home/hulles/Alixia Exec/Home.ics";
		AlixiaApplication.setErrorOnUncommittedObjects(false);
		try (Reader reader = new FileReader(fileName)) {
			ical = Biweekly.parse(reader).first();
			LOGGER.log(LOGLEVEL, "ToDo's:");
			todos = ical.getTodos();
			for (VTodo vt : todos) {
                summary = vt.getSummary();
				LOGGER.log(LOGLEVEL, "To Do: {0}", summary.getValue());
				task = Task.createNew();
				task.setDescription(summary.getValue());
				task.setPerson(person);
				task.setTaskPriority(taskPriority);
				task.setTaskType(taskType);
				if (vt.getCompleted() != null) {
					completed = vt.getCompleted();
					date = completed.getValue();
					LOGGER.log(LOGLEVEL, "Completed Date: {0}", date);
					ldt = AlixiaUtils.ldtFromUtilDate(date);
					task.setDateCompleted(ldt);
				}
				if (vt.getCreated() != null) {
					created = vt.getCreated();
					date = created.getValue();
					LOGGER.log(LOGLEVEL, "Created Date: {0}", date);
					ldt = AlixiaUtils.ldtFromUtilDate(date);
					task.setDateCreated(ldt);
				}
				if (vt.getDateDue() != null) {
					dateDue = vt.getDateDue();
					date = dateDue.getValue();
					LOGGER.log(LOGLEVEL, "Date Due: {0}", date);
					ldt = AlixiaUtils.ldtFromUtilDate(date);
					task.setDateDue(ldt);
				}
				if (vt.getStatus() != null) {
					status = vt.getStatus();
                    statusValue = status.getValue();
					LOGGER.log(LOGLEVEL, "Status: {0}", statusValue);
                    switch (statusValue) {
                        case "COMPLETED":
                            taskStatus = TaskStatus.findTaskStatus(2);
                            task.setTaskStatus(taskStatus);
                            break;
                        case "IN-PROCESS":
                            taskStatus = TaskStatus.findTaskStatus(1);
                            task.setTaskStatus(taskStatus);
                            break;
                        default:
                            throw new AlixiaException();
                    }
				}
				if (vt.getUid() != null) {
					uid = vt.getUid();
					LOGGER.log(LOGLEVEL, "Uid: {0}", uid);
					task.setTaskUuid(uid.getValue());
				}
				task.commit();
			}
			LOGGER.log(LOGLEVEL, "Events:");
			events = ical.getEvents();
			for (VEvent event : events) {
				eventSummary = event.getSummary().getValue();
				LOGGER.log(LOGLEVEL, "Event: {0}", eventSummary);
			}
		} catch (FileNotFoundException e) {
			throw new AlixiaException("LoadTasks: can't find calendar file", e);
		} catch (IOException e1) {
			throw new AlixiaException("LoadTasks: can't close calendar file", e1);
		}
		AlixiaApplication.setErrorOnUncommittedObjects(false);
	}
	
	public static void main(String[] args) {
		
		loadTasks();
	}
}
