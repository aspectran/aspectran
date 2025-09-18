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
package com.aspectran.web.support.http;

import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.cache.Cache;
import com.aspectran.utils.cache.ConcurrentLruCache;

import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Miscellaneous {@link MediaType} utility methods.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author Dimitrios Liapis
 * @author Brian Clozel
 */
public abstract class MediaTypeUtils {

    private static final Cache<String, MediaType> cachedMimeTypes =
        new ConcurrentLruCache<>(64, MediaTypeUtils::parseMediaTypeInternal);

    /**
     * Parse the given String into a single {@code MediaType}.
     * Recently parsed {@code MediaType} are cached for further retrieval.
     * @param mediaType the string to parse
     * @return the media type
     * @throws InvalidMediaTypeException if the string cannot be parsed
     */
    protected static MediaType parseMediaType(String mediaType) {
        if (!StringUtils.hasLength(mediaType)) {
            throw new InvalidMediaTypeException(mediaType, "'mediaType' must not be empty");
        }
        // do not cache multipart mime types with random boundaries
        if (mediaType.startsWith("multipart")) {
            return parseMediaTypeInternal(mediaType);
        }
        return cachedMimeTypes.get(mediaType);
    }

    @NonNull
    private static MediaType parseMediaTypeInternal(@NonNull String mediaType) {
        int index = mediaType.indexOf(';');
        String fullType = (index >= 0 ? mediaType.substring(0, index) : mediaType).trim();
        if (fullType.isEmpty()) {
            throw new InvalidMediaTypeException(mediaType, "'mediaType' must not be empty");
        }

        // java.net.HttpURLConnection returns a *; q=.2 Accept header
        if (MediaType.WILDCARD_TYPE.equals(fullType)) {
            fullType = MediaType.ALL_VALUE;
        }
        int subIndex = fullType.indexOf('/');
        if (subIndex == -1) {
            throw new InvalidMediaTypeException(mediaType, "does not contain '/'");
        }
        if (subIndex == fullType.length() - 1) {
            throw new InvalidMediaTypeException(mediaType, "does not contain subtype after '/'");
        }
        String type = fullType.substring(0, subIndex);
        String subtype = fullType.substring(subIndex + 1);
        if (MediaType.WILDCARD_TYPE.equals(type) && !MediaType.WILDCARD_TYPE.equals(subtype)) {
            throw new InvalidMediaTypeException(mediaType, "wildcard type is legal only in '*/*' (all media types)");
        }

        Map<String, String> parameters = null;
        do {
            int nextIndex = index + 1;
            boolean quoted = false;
            while (nextIndex < mediaType.length()) {
                char ch = mediaType.charAt(nextIndex);
                if (ch == ';') {
                    if (!quoted) {
                        break;
                    }
                } else if (ch == '"') {
                    quoted = !quoted;
                }
                nextIndex++;
            }
            String parameter = mediaType.substring(index + 1, nextIndex).trim();
            if (!parameter.isEmpty()) {
                if (parameters == null) {
                    parameters = new LinkedHashMap<>(4);
                }
                int eqIndex = parameter.indexOf('=');
                if (eqIndex >= 0) {
                    String attribute = parameter.substring(0, eqIndex).trim();
                    String value = parameter.substring(eqIndex + 1).trim();
                    parameters.put(attribute, value);
                }
            }
            index = nextIndex;
        } while (index < mediaType.length());

        try {
            return new MediaType(type, subtype, parameters);
        } catch (UnsupportedCharsetException ex) {
            throw new InvalidMediaTypeException(mediaType, "unsupported charset '" + ex.getCharsetName() + "'");
        } catch (IllegalArgumentException ex) {
            throw new InvalidMediaTypeException(mediaType, ex.getMessage());
        }
    }

    /**
     * Tokenize the given comma-separated string of {@code MediaType} objects
     * into a {@code List<String>}. Unlike simple tokenization by ",", this
     * method takes into account quoted parameters.
     * @param mediaTypes the string to tokenize
     * @return the list of tokens
     */
    @NonNull
    public static List<String> tokenize(String mediaTypes) {
        if (!StringUtils.hasLength(mediaTypes)) {
            return Collections.emptyList();
        }
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        int startIndex = 0;
        int i = 0;
        while (i < mediaTypes.length()) {
            switch (mediaTypes.charAt(i)) {
                case '"' -> inQuotes = !inQuotes;
                case ',' -> {
                    if (!inQuotes) {
                        tokens.add(mediaTypes.substring(startIndex, i).trim());
                        startIndex = i + 1;
                    }
                }
                case '\\' -> i++;
            }
            i++;
        }
        tokens.add(mediaTypes.substring(startIndex).trim());
        return tokens;
    }

    /**
     * Return a string representation of the given list of {@code MediaType} objects.
     * @param mediaTypes the string to parse
     * @return the list of media types
     * @throws IllegalArgumentException if the String cannot be parsed
     */
    @NonNull
    public static String toString(@NonNull Collection<MediaType> mediaTypes) {
        StringBuilder builder = new StringBuilder();
        for (Iterator<? extends MediaType> iterator = mediaTypes.iterator(); iterator.hasNext(); ) {
            MediaType mediaType = iterator.next();
            mediaType.appendTo(builder);
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

}
