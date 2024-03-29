/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
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
 *******************************************************************************/
package com.hulles.fortuna.cayenne;

import java.util.Collection;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;

public class FortunaApplication {
    private static ObjectContext entityContext = null;
	private static ServerRuntime cayenneRuntime = null;
    private static boolean uncommittedObjectsError = true;
    
	private FortunaApplication() {
		// only static methods now...
	}
	
    /**
     * Return the project's Cayenne ServerRuntime object, for use in creating local ObjectContexts e.g.
     * 
     * @return The Cayenne ServerRuntime
     */
    private synchronized static ServerRuntime getServerRuntime() {
    	
    	if (cayenneRuntime == null) {
            cayenneRuntime = ServerRuntime.builder()
                    .addConfig("com/hulles/fortuna/cayenne/cayenne-fortuna.xml")
                    .build();
    	}
    	return cayenneRuntime;
    }

    public static void setErrorOnUncommittedObjects(boolean value) {
    	
    	uncommittedObjectsError = value;
    }
    
    /**
     * Return an instance of ObjectContext for use by the various Cayenne entities. It needs to be 
     * the same for all entities to avoid keeping track of contexts on an individual entity basis. 
     * For example, you can't update an object dependency if the dependent object is in a 
     * different context.
     *  
     * @return A Cayenne ObjectContext
     */
    public synchronized static ObjectContext getEntityContext() {
    	Collection<?> objects;
    	
    	if (entityContext == null) {
    		entityContext = getServerRuntime().newContext();
    	}
    	if (uncommittedObjectsError) {
	    	if (entityContext.hasChanges()) {
	    		objects = entityContext.uncommittedObjects();
	    		for (Object object : objects) {
	    			java.lang.System.err.println("Uncommitted Object " + object.toString());
	    		}
	    		throw new RuntimeException("Cayenne entity context has changes pending");
	    	}
    	}
    	return entityContext;
    }

	public static boolean isProductionServer() {
		
		// TODO fix this when we go to production
		return false;
	}
	
	public static void rollBack() {
		
		getEntityContext().rollbackChanges();
	}
	
	public static void commit() {
		
		getEntityContext().commitChanges();
	}

}
