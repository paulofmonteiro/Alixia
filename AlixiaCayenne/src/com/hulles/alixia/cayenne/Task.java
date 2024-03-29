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
package com.hulles.alixia.cayenne;

import java.util.List;
import java.util.UUID;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

import com.hulles.alixia.api.shared.SerialPerson;
import com.hulles.alixia.api.shared.SerialUUID;
import com.hulles.alixia.api.shared.SharedUtils;
import com.hulles.alixia.cayenne.auto._Task;

public class Task extends _Task {
    private static final long serialVersionUID = 1L; 
    
    public static Task findTask(Integer taskID) {
		ObjectContext context;
		Task task;
		
		SharedUtils.checkNotNull(taskID);
		context = AlixiaApplication.getEntityContext();
		task = Cayenne.objectForPK(context, Task.class, taskID);
		return task;
    }
    
	public static List<Task> getAllTasks() {
		ObjectContext context;
		List<Task> dbTasks;
		
		context = AlixiaApplication.getEntityContext();
		dbTasks = ObjectSelect
				.query(Task.class)
				.orderBy("dateDue", SortOrder.DESCENDING)
				.select(context);
		return dbTasks;
    }
    
	public static List<Task> getTasks(SerialUUID<SerialPerson> personUUID) {
		Ordering ordering;
		List<Task> dbTasks;
		Person person;
		
		SharedUtils.checkNotNull(personUUID);
		person = Person.findPerson(personUUID);
		if (person == null) {
			return null;
		}
		ordering = new Ordering("dateDue", SortOrder.DESCENDING);
		dbTasks = person.getTasks();
		ordering.orderList(dbTasks);
		return dbTasks;
    }
    
	public static List<Task> getUncompletedTasks(SerialUUID<SerialPerson> personUUID) {
		ObjectContext context;
		List<Task> dbTasks;
		Person person;
		TaskStatus completedStatus;
		
		SharedUtils.checkNotNull(personUUID);
		person = Person.findPerson(personUUID);
		if (person == null) {
			return null;
		}
		completedStatus = TaskStatus.getCompletedStatus();
		context = AlixiaApplication.getEntityContext();
		dbTasks = ObjectSelect
				.query(Task.class)
				.where(_Task.PERSON.eq(person)
						.andExp(_Task.TASK_STATUS.ne(completedStatus)))
				.orderBy("dateDue", SortOrder.DESCENDING)
				.select(context);
		return dbTasks;
    }
	
/*    public static List<Task> getTask(String word, String pos) {
		ObjectContext context;
		List<Task> dbTasks = null;
		
		SharedUtils.checkNotNull(word);
		SharedUtils.checkNotNull(pos);
		context = AlixiaApplication.getEntityContext();
		dbTasks = ObjectSelect
				.query(Task.class)
				.where(_Task.WORD.eq(word)
						.andExp(_Task.POS.eq(pos))
						.andExp(_Task.LEMMA.ne(DUMMY_LEMMA)))
				.select(context);
		return dbTasks;
    }
*/    
    public void commit() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.commitChanges();
    }
    
    public void rollback() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.rollbackChanges();
    }

	public void delete() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
     	context.deleteObjects(this);
    	context.commitChanges();
	}

	public static Task createNew() {
    	ObjectContext context;
    	Task dbTask;
    	
    	context = AlixiaApplication.getEntityContext();
        dbTask = context.newObject(Task.class);
        dbTask.setTaskUuid(UUID.randomUUID().toString());
    	// NOT committed yet
    	return dbTask;
	}
}
