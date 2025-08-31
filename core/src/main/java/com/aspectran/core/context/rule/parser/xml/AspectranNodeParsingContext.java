package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.core.context.rule.assistant.ActivityRuleAssistant;

/**
 * <p>Created: 2025-08-31</p>
 */
class AspectranNodeParsingContext {

    private static final ThreadLocal<AspectranNodeParser> CURRENT = new ThreadLocal<>();

    static void set(AspectranNodeParser parser) {
        CURRENT.set(parser);
    }

    static void clear() {
        CURRENT.remove();
    }

    static AspectranNodeParser current() {
        return CURRENT.get();
    }

    static ActivityRuleAssistant assistant() {
        return CURRENT.get().getAssistant();
    }

    /**
     * Pushes an object onto the internal object stack.
     * This stack is used to manage context objects during parsing.
     * @param object the object to push
     */
    static void pushObject(Object object) {
        current().getObjectStack().push(object);
    }

    /**
     * Pops an object from the top of the internal object stack.
     * @param <T> the expected type of the object
     * @return the object popped from the stack
     */
    @SuppressWarnings("unchecked")
    static <T> T popObject() {
        return (T)current().getObjectStack().pop();
    }

    /**
     * Peeks at the object on the top of the internal object stack without removing it.
     * @param <T> the expected type of the object
     * @return the object at the top of the stack
     */
    @SuppressWarnings("unchecked")
    static <T> T peekObject() {
        return (T)current().getObjectStack().peek();
    }

    /**
     * Peeks at an object at a specific depth from the top of the internal object stack.
     * @param <T> the expected type of the object
     * @param n the depth from the top (0 for top, 1 for next, etc.)
     * @return the object at the specified depth
     */
    @SuppressWarnings("unchecked")
    static <T> T peekObject(int n) {
        return (T)current().getObjectStack().peek(n);
    }

    /**
     * Peeks at an object of a specific type from the internal object stack.
     * It searches the stack from top to bottom for the first object assignable to the target type.
     * @param <T> the expected type of the object
     * @param target the target class type
     * @return the object of the specified type, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    static <T> T peekObject(Class<?> target) {
        return (T)current().getObjectStack().peek(target);
    }

    /**
     * Clears all objects from the internal object stack.
     */
    static void clearObjectStack() {
        current().getObjectStack().clear();
    }

}
