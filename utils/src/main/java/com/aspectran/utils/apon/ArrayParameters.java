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

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Root-level {@link Parameters} implementation representing an array of nameless
 * parameter groups.
 * <p>
 * The synthetic name {@code <noname>} is used internally to store array elements.
 * Provides convenient iteration and accessors for array contents.
 * </p>
 *
 * @since 6.2.0
 */
public class ArrayParameters extends AbstractParameters implements Iterable<Parameters>, Serializable {

    @Serial
    private static final long serialVersionUID = 2058392199376865356L;

    /**
     * The synthetic name for the array parameter.
     */
    public static final String NONAME = "<noname>";

    private final Class<? extends AbstractParameters> elementClass;

    /**
     * Create an array container whose elements are {@link VariableParameters} blocks.
     */
    public ArrayParameters() {
        this(VariableParameters.class);
    }

    /**
     * Parse APON text into a new array container whose elements are {@link VariableParameters}.
     * @param apon APON text representing an array
     * @throws AponParseException if parsing fails
     */
    public ArrayParameters(String apon) throws AponParseException {
        this(VariableParameters.class, apon);
    }

    /**
     * Create an array container with the given element class for each entry.
     * @param elementClass the Parameters implementation for array elements
     */
    public ArrayParameters(Class<? extends AbstractParameters> elementClass) {
        super(createParameterKeys(elementClass));
        this.elementClass = elementClass;
    }

    /**
     * Parse APON text into a new array container with the given element class.
     * @param elementClass the Parameters implementation for array elements
     * @param apon APON text representing an array
     * @throws AponParseException if parsing fails
     */
    public ArrayParameters(Class<? extends AbstractParameters> elementClass, String apon) throws AponParseException {
        this(elementClass);
        readFrom(StringUtils.trimWhitespace(apon));
    }

    /**
     * Append a Parameters block as the next element of this array.
     * @param parameters the element to add
     */
    public void addParameters(Parameters parameters) {
        putValue(NONAME, parameters);
    }

    /**
     * Return the contents as an array of parameter blocks.
     * @param <T> the element type
     * @return the array of elements or {@code null} if none
     */
    public <T extends Parameters> T[] getParametersArray() {
        return getParametersArray(NONAME);
    }

    /**
     * Return the contents as a list of parameter blocks.
     * @param <T> the element type
     * @return the list of elements or {@code null} if none
     */
    public <T extends Parameters> List<T> getParametersList() {
        return getParametersList(NONAME);
    }

    /**
     * Returns an iterator over the parameter blocks contained in this array.
     * @return an iterator, possibly empty
     */
    @Override
    @NonNull
    public Iterator<Parameters> iterator() {
        List<Parameters> list = getParametersList(NONAME);
        if (list != null) {
            return list.iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    /**
     * Create and add a new element Parameters instance under the synthetic noname key.
     * @param <T> the element Parameters type
     * @param name must be {@link #NONAME}
     * @return the created element instance
     * @throws UnknownParameterException if {@code name} is not supported
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Parameters> T newParameters(String name) {
        Parameter p = getParameter(name);
        if (p == null) {
            throw new UnknownParameterException(name, this);
        }
        try {
            T sub = (T)ClassUtils.createInstance(elementClass);
            sub.setProprietor(p);
            p.putValue(sub);
            return sub;
        } catch (Exception e) {
            throw new InvalidParameterValueException("Failed to instantiate " + elementClass, e);
        }
    }

    /**
     * Create the single schema key used by this array container.
     * @param elementClass the Parameters class used for elements
     * @return a one-element key array for internal use
     */
    @NonNull
    private static ParameterKey[] createParameterKeys(Class<? extends AbstractParameters> elementClass) {
        ParameterKey pk = new ParameterKey(NONAME, elementClass, true);
        return new ParameterKey[] { pk };
    }

}
