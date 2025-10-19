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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.parsing.RuleParsingContext;
import com.aspectran.utils.ArrayStack;
import com.aspectran.utils.Assert;

/**
 * A static helper that provides access to the thread-local {@link AspectranNodeParser} context.
 * <p>It uses a {@link ThreadLocal} stack to manage {@link AspectranNodeParser}
 * instances, ensuring that parsing contexts are isolated between threads. This is
 * crucial for handling nested file appends correctly in a multi-threaded environment.</p>
 *
 * <p>Created: 2024-08-31</p>
 */
public class AspectranNodeParsingContext {

    private static final ThreadLocal<ArrayStack<AspectranNodeParser>> PARSERS =
            ThreadLocal.withInitial(ArrayStack::new);

    /**
     * Sets the current parser for the active thread.
     * If a parser is already active, it is pushed onto the stack.
     * @param parser the parser to set as current
     */
    public static void set(AspectranNodeParser parser) {
        Assert.notNull(parser, "parser must not be null");
        PARSERS.get().push(parser);
    }

    /**
     * Clears the current parser for the active thread.
     * If there was a previous parser on the stack, it becomes the current one.
     * If the stack becomes empty, the thread-local variable is removed.
     */
    public static void clear() {
        ArrayStack<AspectranNodeParser> stack = PARSERS.get();
        if (!stack.isEmpty()) {
            stack.pop();
            if (stack.isEmpty()) {
                PARSERS.remove();
            }
        }
    }

    /**
     * Returns the current parser for the active thread.
     * @return the current {@link AspectranNodeParser}
     * @throws IllegalStateException if no parser is currently set
     */
    static AspectranNodeParser current() {
        ArrayStack<AspectranNodeParser> stack = PARSERS.get();
        if (stack.isEmpty()) {
            throw new IllegalStateException("Current parser not set");
        }
        return stack.peek();
    }

    static RuleParsingContext getCurrentRuleParsingContext() {
        return current().getRuleParsingContext();
    }

    /**
     * Pushes an object onto the internal object stack of the current parser.
     * This stack is used to manage context objects during parsing.
     * @param object the object to push
     */
    static void pushObject(Object object) {
        current().getObjectStack().push(object);
    }

    /**
     * Pops an object from the top of the internal object stack of the current parser.
     * @param <T> the expected type of the object
     * @return the object popped from the stack
     */
    @SuppressWarnings("unchecked")
    static <T> T popObject() {
        return (T)current().getObjectStack().pop();
    }

    /**
     * Peeks at the object on the top of the internal object stack of the current parser without removing it.
     * @param <T> the expected type of the object
     * @return the object at the top of the stack
     */
    @SuppressWarnings("unchecked")
    static <T> T peekObject() {
        return (T)current().getObjectStack().peek();
    }

    /**
     * Peeks at an object at a specific depth from the top of the internal object stack of the current parser.
     * @param <T> the expected type of the object
     * @param n the depth from the top (0 for top, 1 for next, etc.)
     * @return the object at the specified depth
     */
    @SuppressWarnings("unchecked")
    static <T> T peekObject(int n) {
        return (T)current().getObjectStack().peek(n);
    }

    /**
     * Peeks at an object of a specific type from the internal object stack of the current parser.
     * It searches the stack from top to bottom for the first object assignable to the target type.
     * @param <T> the expected type of the object
     * @param target the target class type
     * @return the object of the specified type, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    static <T> T peekObject(Class<?> target) {
        return (T)current().getObjectStack().peek(target);
    }

}
