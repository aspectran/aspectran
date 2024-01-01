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
package com.aspectran.web.support.http;

/**
 * Exception thrown from {@link MediaTypeUtils#parseMediaType(String)} in case of
 * encountering an invalid content type specification String.
 *
 * <p>Created: 2019-06-18</p>
 */
public class InvalidMediaTypeException extends IllegalArgumentException {

    private final String mediaType;

    /**
     * Create a new InvalidContentTypeException for the given content type.
     * @param mediaType the offending media type
     * @param message   a detail message indicating the invalid part
     */
    public InvalidMediaTypeException(String mediaType, String message) {
        super("Invalid media type \"" + mediaType + "\": " + message);
        this.mediaType = mediaType;
    }

    /**
     * Return the offending media type.
     * @return the media type
     */
    public String getMediaType() {
        return this.mediaType;
    }

}
