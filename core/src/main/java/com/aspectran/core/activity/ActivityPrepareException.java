/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.activity;

/**
 * Checked exception thrown when an error occurs while preparing an activity.
 * 
 * <p>Created: 2019. 03. 25.</p>
 */
public class ActivityPrepareException extends ActivityException {

    private static final long serialVersionUID = -6964737280809517019L;

    /**
     * Instantiates a new ActivityPrepareException.
     * @param msg the message
     * @param cause the real cause of the exception
     */
    public ActivityPrepareException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
