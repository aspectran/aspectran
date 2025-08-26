/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.component.session;

import com.aspectran.core.component.Component;

import java.util.Set;

/**
 * Represents a persistent store for session data.
 *
 * <p>This interface defines the contract for storing, loading, and deleting
 * session data from a persistent medium, such as a file system or a database.
 * It is a key component for enabling session persistence across server restarts
 * and for session clustering.
 *
 * <p>Created: 2017. 6. 15.</p>
 */
public interface SessionStore extends Component {

    /**
     * Loads session data from the persistent store.
     * @param id the unique identifier of the session to load
     * @return the loaded session data, or {@code null} if the session does not exist
     * @throws Exception if there is an error loading the session data
     */
    SessionData load(String id) throws Exception;

    /**
     * Saves session data to the persistent store.
     * If a session with the same ID already exists, it will be overwritten.
     * @param id the unique identifier of the session to save
     * @param data the session data to be saved
     * @throws Exception if there is an error saving the session data
     */
    void save(String id, SessionData data) throws Exception;

    /**
     * Deletes session data from the persistent store.
     * @param id the unique identifier of the session to delete
     * @return {@code true} if the session was successfully deleted; {@code false} otherwise
     * @throws Exception if there is an error deleting the session data
     */
    boolean delete(String id) throws Exception;

    /**
     * Checks if a session with the specified ID exists in the persistent store.
     * @param id the unique identifier of the session to check
     * @return {@code true} if the session exists; {@code false} otherwise
     * @throws Exception if there is an error checking for the session's existence
     */
    boolean exists(String id) throws Exception;

    /**
     * Finds and returns the set of session IDs that have expired.
     * This method is called periodically by the {@link HouseKeeper} to identify
     * sessions that need to be cleaned up.
     * @param candidates a set of session IDs that are suspected to be expired
     * @return a set of session IDs that have been confirmed as expired
     */
    Set<String> getExpired(Set<String> candidates);

    /**
     * Removes all unmanaged (orphan) sessions that expired at or before the given time.
     * @param time the time before which the sessions must have expired
     */
    void cleanOrphans(long time);

    /**
     * Returns the names of the attributes that should be excluded from serialization.
     * @return the attribute names to be excluded from serialization
     */
    Set<String> getNonPersistentAttributes();

    /**
     * Retrieves the set of all session IDs present in the store.
     * @return a set containing all session IDs
     */
    Set<String> getAllSessions();

}
