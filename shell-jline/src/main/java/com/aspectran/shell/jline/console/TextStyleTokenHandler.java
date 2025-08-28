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
package com.aspectran.shell.jline.console;

import com.aspectran.utils.StringUtils;

/**
 * A token handler for parsing text with inline style tags like <code>{{style1,style2}}</code>.
 * This class processes a character sequence, distinguishing between literal text
 * and style specifications.
 *
 * <p>Created: 2017. 11. 19.</p>
 */
public abstract class TextStyleTokenHandler {

    /**
     * Handles a literal character.
     * @param c the character to handle
     */
    public abstract void character(char c);

    /**
     * Handles a style specification.
     * @param styles an array of style names
     */
    public abstract void style(String... styles);

    /**
     * Parses the input character sequence to process text and style tokens.
     * <p>
     * Style tokens are enclosed in double curly braces, for example, <code>{{bold,red}}</code>.
     * A single open brace <code>{</code> is treated as a literal character.
     * An unclosed style token at the end of the input is treated as literal text.
     * </p>
     * @param input the character sequence to parse
     */
    public void handle(CharSequence input) {
        if (input == null) {
            return;
        }

        final int STATE_DEFAULT = 0;
        final int STATE_SEEN_ONE_OPEN_BRACE = 1;
        final int STATE_IN_STYLE_TAG = 2;

        int state = STATE_DEFAULT;
        int styleContentStart = -1;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (state) {
                case STATE_DEFAULT:
                    if (c == '{') {
                        state = STATE_SEEN_ONE_OPEN_BRACE;
                    } else {
                        character(c);
                    }
                    break;
                case STATE_SEEN_ONE_OPEN_BRACE:
                    if (c == '{') {
                        state = STATE_IN_STYLE_TAG;
                        styleContentStart = i + 1;
                    } else {
                        // Not a tag, was a literal '{' followed by another char
                        character('{');
                        character(c);
                        state = STATE_DEFAULT;
                    }
                    break;
                case STATE_IN_STYLE_TAG:
                    if (c == '}') {
                        // Check for "}}"
                        if (i + 1 < input.length() && input.charAt(i + 1) == '}') {
                            // End of tag found
                            String styleSpec = input.subSequence(styleContentStart, i).toString();
                            String[] styles = StringUtils.splitWithComma(styleSpec);
                            style(styles);
                            i++; // consume the second '}'
                            state = STATE_DEFAULT;
                            styleContentStart = -1;
                        }
                        // A single '}' inside a tag is treated as part of the style spec,
                        // so we do nothing here and let it be part of the substring.
                    }
                    break;
            }
        }

        // Handle unclosed tags or pending characters at the end of the input
        if (state == STATE_SEEN_ONE_OPEN_BRACE) {
            character('{');
        } else if (state == STATE_IN_STYLE_TAG) {
            character('{');
            character('{');
            for (int i = styleContentStart; i < input.length(); i++) {
                character(input.charAt(i));
            }
        }
    }

}
