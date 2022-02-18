/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
 * Thrown when an error occurs while performing an instant activity.
 */
public class InstantActivityException extends RuntimeException {

    private static final long serialVersionUID = 3431407337587193795L;

    /**
     * Instantiates a new InstantActivityException.
     *
     * @param cause the real cause of the exception
     */
    public InstantActivityException(Throwable cause) {
        super("An error occurred while performing an instant activity", cause);
    }

}
