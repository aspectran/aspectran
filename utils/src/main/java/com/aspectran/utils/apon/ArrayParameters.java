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

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A Root Parameters to Represent an Array of Nameless Parameters.
 *
 * @since 6.2.0
 */
public class ArrayParameters extends AbstractParameters implements Iterable<Parameters>, Serializable {

    @Serial
    private static final long serialVersionUID = 2058392199376865356L;

    public static final String NONAME = "<noname>";

    private final Class<? extends AbstractParameters> elementClass;

    public ArrayParameters() {
        this(VariableParameters.class);
    }

    public ArrayParameters(String apon) throws AponParseException {
        this(VariableParameters.class, apon);
    }

    public ArrayParameters(Class<? extends AbstractParameters> elementClass) {
        super(createParameterKeys(elementClass));
        this.elementClass = elementClass;
    }

    public ArrayParameters(Class<? extends AbstractParameters> elementClass, String apon) throws AponParseException {
        this(elementClass);
        readFrom(StringUtils.trimWhitespace(apon));
    }

    public void addParameters(Parameters parameters) {
        putValue(NONAME, parameters);
    }

    public <T extends Parameters> T[] getParametersArray() {
        return getParametersArray(NONAME);
    }

    public <T extends Parameters> List<T> getParametersList() {
        return getParametersList(NONAME);
    }

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

    @NonNull
    private static ParameterKey[] createParameterKeys(Class<? extends AbstractParameters> elementClass) {
        ParameterKey pk = new ParameterKey(NONAME, elementClass, true);
        return new ParameterKey[] { pk };
    }

}
