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
package com.aspectran.utils.apon;

import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.StringWriter;
import java.util.Arrays;

/**
 * A utility class for conveniently writing text in APON format.
 *
 * @since 7.4.3
 */
public class AponLines extends AponFormat {

    /**
     * Internal writer state indicating the currently open structure.
     */
    private enum State {
        /** Inside a block delimited by curly braces: { } */
        BLOCK,
        /** Inside an array delimited by square brackets: [ ] */
        ARRAY,
        /** Inside a multi-line text value delimited by round brackets: ( ) */
        TEXT
    }

    /**
     * Buffer accumulating APON lines as they are appended via the fluent API.
     */
    private final StringWriter lines = new StringWriter();

    /**
     * Stack tracking nested writer states to validate operations and emit closing brackets.
     */
    private final ArrayStack<State> stateStack = new ArrayStack<>();

    /**
     * Create a new builder with a top-level block started.
     */
    public AponLines() {
        block();
    }

    /**
     * Append a raw line of APON content to the current context.
     * When inside a text block, a leading '|' marker is inserted automatically.
     * @param line the content line to append; ignored if {@code null}
     * @return this builder for chaining
     */
    public AponLines line(String line) {
        if (stateStack.isEmpty()) {
            // This path should not be taken given the constructor initializes the state.
            throw new IllegalStateException("Cannot add a line to an empty structure.");
        }
        if (line != null) {
            if (stateStack.peek() == State.TEXT) {
                lines.append(TEXT_LINE_START);
            }
            escape(line);
            lines.append(NEW_LINE);
        }
        return this;
    }

    /**
     * Append a name/value pair to the current block.
     * @param name the parameter name (must have text)
     * @param value the value; if {@code null} nothing is written
     * @return this builder for chaining
     * @throws IllegalArgumentException if the name is blank
     * @throws IllegalStateException if not currently in a block context
     */
    public AponLines line(String name, Object value) {
        checkName(name);
        if (stateStack.peek() != State.BLOCK) {
            throw new IllegalStateException("Named parameters can only be added inside a block, " +
                    "not an array or text block");
        }
        if (value != null) {
            lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE);
            escape(value);
            lines.append(NEW_LINE);
        }
        return this;
    }

    /**
     * Begin a named block: name: {
     * @param name the block name
     * @return this builder for chaining
     * @throws IllegalArgumentException if the name is blank
     * @throws IllegalStateException if not currently in a block context
     */
    public AponLines block(String name) {
        checkName(name);
        if (stateStack.peek() != State.BLOCK) {
            throw new IllegalStateException("Named blocks can only be added inside another block, " +
                    "not an array or text block");
        }
        stateStack.push(State.BLOCK);
        lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE).append(CURLY_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    /**
     * Begin an anonymous block within an array element: {
     * or initialize the root block when first constructed.
     * @return this builder for chaining
     * @throws IllegalStateException if not currently in an array when nested
     */
    public AponLines block() {
        if (stateStack.isEmpty()) {
            stateStack.push(State.BLOCK);
        } else {
            if (stateStack.peek() != State.ARRAY) {
                throw new IllegalStateException("Anonymous blocks can only be added inside an array. " +
                        "To add a named block, use block(\"name\").");
            }
            stateStack.push(State.BLOCK);
            lines.append(CURLY_BRACKET_OPEN).append(NEW_LINE);
        }
        return this;
    }

    /**
     * Begin a named array: name: [
     * @param name the array name
     * @return this builder for chaining
     * @throws IllegalArgumentException if the name is blank
     * @throws IllegalStateException if not currently in a block context
     */
    public AponLines array(String name) {
        checkName(name);
        if (stateStack.peek() != State.BLOCK) {
            throw new IllegalStateException("Named arrays can only be added inside a block, " +
                    "not an array or text block");
        }
        stateStack.push(State.ARRAY);
        lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE).append(SQUARE_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    /**
     * Begin an anonymous array element: [
     * @return this builder for chaining
     * @throws IllegalStateException if not currently in an array context
     */
    public AponLines array() {
        if (stateStack.peek() != State.ARRAY) {
            throw new IllegalStateException("Anonymous arrays can only be added inside another array. " +
                    "To add a named array, use array(\"name\").");
        }
        stateStack.push(State.ARRAY);
        lines.append(SQUARE_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    /**
     * Begin a named multi-line text value: name: (
     * Lines should then be added via {@link #line(String)} and will be prefixed with '|'.
     * @param name the parameter name
     * @return this builder for chaining
     * @throws IllegalArgumentException if the name is blank
     * @throws IllegalStateException if not currently in a block context
     */
    public AponLines text(String name) {
        checkName(name);
        if (stateStack.peek() != State.BLOCK) {
            throw new IllegalStateException("Named text blocks can only be added inside a block, " +
                    "not an array or another text block");
        }
        stateStack.push(State.TEXT);
        lines.append(name).append(NAME_VALUE_SEPARATOR).append(SPACE).append(ROUND_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    /**
     * Begin an anonymous multi-line text value: (
     * @return this builder for chaining
     * @throws IllegalStateException if not currently in an array context
     */
    public AponLines text() {
        if (stateStack.peek() != State.ARRAY) {
            throw new IllegalStateException("Anonymous text blocks can only be added inside an array. " +
                    "To add a named text block, use text(\"name\").");
        }
        stateStack.push(State.TEXT);
        lines.append(ROUND_BRACKET_OPEN).append(NEW_LINE);
        return this;
    }

    /**
     * Ends the most recently started structure (block, array, or text).
     * Appends the corresponding closing bracket and a newline.
     * @return this builder for chaining
     * @throws IllegalStateException if there is no open structure to end
     */
    public AponLines end() {
        if (stateStack.size() <= 1) {
            throw new IllegalStateException("Mismatched end() call; no open block, array, or text to close");
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

    private void escape(@NonNull Object object) {
        if (object instanceof String str && (
                str.indexOf(DOUBLE_QUOTE_CHAR) >= 0 ||
                        str.indexOf(SINGLE_QUOTE_CHAR) >= 0 ||
                        str.startsWith(SPACE) ||
                        str.endsWith(SPACE) ||
                        str.contains(NEW_LINE))) {
            lines.append(DOUBLE_QUOTE_CHAR);
            lines.append(AponWriter.escape(str));
            lines.append(DOUBLE_QUOTE_CHAR);
        } else {
            lines.append(String.valueOf(object));
        }
    }

    @Override
    public String toString() {
        return lines.toString();
    }

    /**
     * Parses the built APON text and re-emits it using the default formatting rules.
     * Useful for producing a normalized string from content assembled with this builder.
     * @return the formatted APON string
     * @throws AponParseException if the current content cannot be parsed as APON
     */
    public String format() throws AponParseException {
        return AponReader.read(toString()).toString();
    }

    private void checkName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Parameter name must not be null or empty");
        }
    }

}
