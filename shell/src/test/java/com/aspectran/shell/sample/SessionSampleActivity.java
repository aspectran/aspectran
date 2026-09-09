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
package com.aspectran.shell.sample;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.SessionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Sample activity that demonstrates how to use the built-in session management
 * of Aspectran Shell via {@link SessionAdapter}.
 *
 * <p>The session is managed by the shell service's built-in {@code SessionManager},
 * which is configured in the {@code shell.session} section of the Aspectran config.
 * Each shell command execution shares the same session through the {@link SessionAdapter}
 * provided by {@link Translet#getSessionAdapter()}.</p>
 *
 * <p>Provides the following operations:
 * <ul>
 *   <li>{@link #login(Translet)}       - Stores user info in the session.</li>
 *   <li>{@link #logout(Translet)}      - Invalidates the current session.</li>
 *   <li>{@link #whoami(Translet)}      - Prints info about the currently logged-in user.</li>
 *   <li>{@link #sessionInfo(Translet)} - Prints detailed session metadata.</li>
 * </ul>
 * </p>
 */
public class SessionSampleActivity {

    private static final String ATTR_USERNAME = "username";
    private static final String ATTR_ROLE = "role";
    private static final String ATTR_LOGIN_TIME = "loginTime";

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Logger logger = LoggerFactory.getLogger(SessionSampleActivity.class);

    /**
     * Logs in with the given username and role, and stores the information in the session.
     *
     * @param translet the translet carrying the request parameters
     * @return a login result message
     */
    public String login(Translet translet) {
        if (!translet.hasSessionAdapter()) {
            return "ERROR: Session support is not enabled. " +
                    "Please configure 'shell.session' in the Aspectran config.";
        }

        SessionAdapter session = translet.getSessionAdapter();

        String existingUser = session.getAttribute(ATTR_USERNAME);
        if (existingUser != null) {
            return "Already logged in as '" + existingUser + "'. Please logout first.";
        }

        String username = translet.getParameter("username");
        String role = translet.getParameter("role");

        if (username == null || username.isBlank()) {
            return "ERROR: username is required.";
        }
        if (role == null || role.isBlank()) {
            role = "USER";
        }

        session.setAttribute(ATTR_USERNAME, username);
        session.setAttribute(ATTR_ROLE, role.toUpperCase());
        session.setAttribute(ATTR_LOGIN_TIME, System.currentTimeMillis());

        logger.info("User '{}' logged in with role '{}', sessionId={}",
                username, role, session.getId());

        return String.format("""
                -------------------------------------------------------
                Login successful!
                  Username   : %s
                  Role       : %s
                  Session ID : %s
                -------------------------------------------------------""",
                username, role.toUpperCase(), session.getId());
    }

    /**
     * Logs out the currently logged-in user by invalidating the current session.
     *
     * @param translet the translet carrying the request parameters
     * @return a logout result message
     */
    public String logout(Translet translet) {
        if (!translet.hasSessionAdapter()) {
            return "ERROR: Session support is not enabled.";
        }

        SessionAdapter session = translet.getSessionAdapter();

        String username = session.getAttribute(ATTR_USERNAME);
        if (username == null) {
            return "You are not logged in.";
        }

        String sessionId = session.getId();
        session.invalidate();

        logger.info("User '{}' logged out, sessionId={}", username, sessionId);

        return String.format("""
                -------------------------------------------------------
                Logout successful!
                  Username   : %s
                  Session ID : %s (invalidated)
                -------------------------------------------------------""",
                username, sessionId);
    }

    /**
     * Prints information about the currently logged-in user.
     *
     * @param translet the translet carrying the request parameters
     * @return a message showing the current user's login status
     */
    public String whoami(Translet translet) {
        if (!translet.hasSessionAdapter()) {
            return "ERROR: Session support is not enabled.";
        }

        SessionAdapter session = translet.getSessionAdapter();

        String username = session.getAttribute(ATTR_USERNAME);
        if (username == null) {
            return "You are not logged in. Use 'login' to start a session.";
        }

        String role = session.getAttribute(ATTR_ROLE);
        Long loginTimeMillis = session.getAttribute(ATTR_LOGIN_TIME);
        String loginTime = (loginTimeMillis != null)
                ? LocalDateTime.ofInstant(Instant.ofEpochMilli(loginTimeMillis), ZoneId.systemDefault())
                        .format(DATE_TIME_FORMATTER)
                : "N/A";

        return String.format("""
                -------------------------------------------------------
                Current User
                  Username   : %s
                  Role       : %s
                  Login Time : %s
                -------------------------------------------------------""",
                username, role, loginTime);
    }

    /**
     * Prints detailed metadata about the current session.
     *
     * @param translet the translet carrying the request parameters
     * @return a formatted string of session metadata
     */
    public String sessionInfo(Translet translet) {
        if (!translet.hasSessionAdapter()) {
            return "ERROR: Session support is not enabled.";
        }

        SessionAdapter session = translet.getSessionAdapter();

        String creationTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(session.getCreationTime()), ZoneId.systemDefault())
                .format(DATE_TIME_FORMATTER);
        String lastAccessedTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(session.getLastAccessedTime()), ZoneId.systemDefault())
                .format(DATE_TIME_FORMATTER);

        String username = session.getAttribute(ATTR_USERNAME);
        String role = session.getAttribute(ATTR_ROLE);

        return String.format("""
                -------------------------------------------------------
                Session Information
                  Session ID        : %s
                  Username          : %s
                  Role              : %s
                  Created At        : %s
                  Last Accessed At  : %s
                  Max Idle Interval : %d seconds
                  Is New            : %s
                  Is Valid          : %s
                -------------------------------------------------------""",
                session.getId(),
                username != null ? username : "(not logged in)",
                role != null ? role : "N/A",
                creationTime,
                lastAccessedTime,
                session.getMaxInactiveInterval(),
                session.isNew(),
                session.isValid());
    }

}
