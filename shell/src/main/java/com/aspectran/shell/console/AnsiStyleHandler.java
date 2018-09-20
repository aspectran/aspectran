/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.shell.console;

import com.aspectran.core.util.StringUtils;

/**
 * <p>Created: 2017. 11. 19.</p>
 */
public abstract class AnsiStyleHandler {

    public abstract void character(char c);

    public abstract void attribute(String... attrs);

    public void handle(CharSequence input) {
        if (input == null) {
            return;
        }

        int inputLen = input.length();
        char c;
        int p1 = 0;
        int p2 = 0;
        int p3 = 0;

        for (int i = 0; i < inputLen; i++) {
            c = input.charAt(i);
            switch (c) {
                case '{':
                    if (p1 < 2) {
                        p1++;
                    } else if (p1 == 2) {
                        character(c);
                    }
                    break;
                case '}':
                    if (p1 >= 2) {
                        if (p2 == 0) {
                            p2++;
                        } else if (p2 == 1) {
                            p2 = i - 1;
                        }
                    } else if (p1 == 1) {
                        character('{');
                        p1 = 0;
                    }
                    break;
                default:
                    if (p1 == 1) {
                        p1 = 0;
                        character('{');
                    } else if (p1 == 2 && p3 == 0) {
                        p1 = p3 = i;
                    }
            }
            if (p1 == 0) {
                character(c);
            } else if (p1 >= 2 && p1 < p2) {
                String[] attrs = StringUtils.splitCommaDelimitedString(input.subSequence(p1, p2).toString());
                attribute(attrs);
                p1 = p2 = p3 = 0;
            }
        }

        if (p1 > 0) {
            for (int i = 0; i < p1; i++) {
                character('{');
            }
            String attr = input.subSequence(p1, inputLen).toString();
            attribute(attr);
        }
    }

}
