/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.utils.apon;

import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.StringUtils;

import java.util.Arrays;

/**
 * A utility class for conveniently writing text in APON format.
 *
 * @since 7.4.3
 */
public class AponLines extends AponFormat {

    private enum State {
        BLOCK,
        ARRAY,
        TEXT
    }

    private final StringBuilder lines = new StringBuilder();

    private final ArrayStack<State> stateStack = new ArrayStack<>();

    public AponLines() {
        block();
    }

    public AponLines line(String line) {
        if (stateStack.isEmpty()) {
            checkState(State.BLOCK);
        }
        if (line != null) {
            if (stateStack.peek() == State.TEXT) {
                lines.append(TEXT_LINE_START);
            }
            lines.append(line).append(NEW_LINE);
        }
        return this;
    }

    public AponLines line(String name, Object value) {
        checkName(name);
        checkState(State.BLOCK);
        if (value != null) {
            lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE).append(value).append(NEW_LINE);
        }
        return this;
    }

    public AponLines block(String name) {
        checkName(name);
        checkState(State.BLOCK);
        stateStack.push(State.BLOCK);
        lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE).append(CURLY_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    public AponLines block() {
        if (stateStack.isEmpty()) {
            stateStack.push(State.BLOCK);
        } else {
            checkState(State.ARRAY);
            stateStack.push(State.BLOCK);
            lines.append(CURLY_BRACKET_OPEN).append(NEW_LINE);
        }
        return this;
    }

    public AponLines array(String name) {
        checkName(name);
        checkState(State.BLOCK);
        stateStack.push(State.ARRAY);
        lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE).append(SQUARE_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    public AponLines array() {
        checkState(State.ARRAY);
        stateStack.push(State.ARRAY);
        lines.append(SQUARE_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    public AponLines text(String name) {
        checkName(name);
        checkState(State.BLOCK);
        stateStack.push(State.TEXT);
        lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE).append(ROUND_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    public AponLines text() {
        checkState(State.ARRAY);
        stateStack.push(State.TEXT);
        lines.append(ROUND_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    public AponLines end() {
        if (stateStack.isEmpty()) {
            checkState(State.BLOCK);
        }
        State state = stateStack.pop();
        switch (state) {
            case BLOCK:
                if (!stateStack.isEmpty()) {
                    lines.append(CURLY_BRACKET_CLOSE).append(NEW_LINE);
                }
                break;
            case ARRAY:
                lines.append(SQUARE_BRACKET_CLOSE).append(NEW_LINE);
                break;
            case TEXT:
                lines.append(ROUND_BRACKET_CLOSE).append(NEW_LINE);
                break;
            default:
                throw new IllegalStateException("Must be one of these states: " + Arrays.toString(State.values()));
        }
        return this;
    }

    @Override
    public String toString() {
        return lines.toString();
    }

    public String format() throws AponParseException {
        return AponReader.read(toString()).toString();
    }

    private void checkName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Invalid name: " + name);
        }
    }

    private void checkState(State required) {
        if (stateStack.isEmpty() || stateStack.peek() != required) {
            throw new IllegalStateException("Required state: " + required);
        }
    }

}
