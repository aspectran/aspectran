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
package com.aspectran.web.support.util;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>This class is a clone of org.springframework.web.util.HtmlCharacterEntityReferences</p>
 *
 * Represents a set of character entity references defined by the
 * HTML 4.0 standard.
 *
 * <p>A complete description of the HTML 4.0 character set can be found
 * at https://www.w3.org/TR/html4/charset.html.
 */
class HtmlCharacterEntityReferences {

    private static final String PROPERTIES_FILE = "HtmlCharacterEntityReferences.properties";

    static final char REFERENCE_START = '&';

    static final String DECIMAL_REFERENCE_START = "&#";

    static final String HEX_REFERENCE_START = "&#x";

    static final char REFERENCE_END = ';';

    static final char CHAR_NULL = (char)-1;

    private final String[] characterToEntityReferenceMap = new String[3000];

    private final Map<String, Character> entityReferenceToCharacterMap = new HashMap<>(512);

    /**
     * Returns a new set of character entity references reflecting the HTML 4.0 character set.
     */
    public HtmlCharacterEntityReferences() {
        Properties entityReferences = new Properties();

        // Load reference definition file
        InputStream is = HtmlCharacterEntityReferences.class.getResourceAsStream(PROPERTIES_FILE);
        if (is == null) {
            throw new IllegalStateException(
                    "Cannot find reference definition file [HtmlCharacterEntityReferences.properties] as class path resource");
        }
        try {
            try {
                entityReferences.load(is);
            } finally {
                is.close();
            }
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Failed to parse reference definition file [HtmlCharacterEntityReferences.properties]: " +  ex.getMessage());
        }

        // Parse reference definition properties
        Enumeration<?> keys = entityReferences.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            int referredChar = Integer.parseInt(key);
            Assert.isTrue((referredChar < 1000 || (referredChar >= 8000 && referredChar < 10000)),
                    () -> "Invalid reference to special HTML entity: " + referredChar);
            int index = (referredChar < 1000 ? referredChar : referredChar - 7000);
            String reference = entityReferences.getProperty(key);
            characterToEntityReferenceMap[index] = REFERENCE_START + reference + REFERENCE_END;
            entityReferenceToCharacterMap.put(reference, (char) referredChar);
        }
    }

    /**
     * Return the number of supported entity references.
     */
    public int getSupportedReferenceCount() {
        return this.entityReferenceToCharacterMap.size();
    }

    /**
     * Return true if the given character is mapped to a supported entity reference.
     */
    public boolean isMappedToReference(char character) {
        return isMappedToReference(character, HtmlUtils.DEFAULT_CHARACTER_ENCODING);
    }

    /**
     * Return true if the given character is mapped to a supported entity reference.
     */
    public boolean isMappedToReference(char character, String encoding) {
        return (convertToReference(character, encoding) != null);
    }

    /**
     * Return the reference mapped to the given character, or {@code null} if none found.
     */
    @Nullable
    public String convertToReference(char character) {
        return convertToReference(character, HtmlUtils.DEFAULT_CHARACTER_ENCODING);
    }

    /**
     * Return the reference mapped to the given character, or {@code null} if none found.
     * @since 4.1.2
     */
    @Nullable
    public String convertToReference(char character, @NonNull String encoding) {
        if (encoding.startsWith("UTF-")) {
            switch (character){
                case '<':
                    return "&lt;";
                case '>':
                    return "&gt;";
                case '"':
                    return "&quot;";
                case '&':
                    return "&amp;";
                case '\'':
                    return "&#39;";
            }
        } else if (character < 1000 || (character >= 8000 && character < 10000)) {
            int index = (character < 1000 ? character : character - 7000);
            return characterToEntityReferenceMap[index];
        }
        return null;
    }

    /**
     * Return the char mapped to the given entityReference or -1.
     */
    public char convertToCharacter(String entityReference) {
        Character referredCharacter = entityReferenceToCharacterMap.get(entityReference);
        return (referredCharacter != null ? referredCharacter : CHAR_NULL);
    }

}
