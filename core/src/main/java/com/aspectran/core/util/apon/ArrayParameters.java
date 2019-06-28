/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.apon;

import com.aspectran.core.util.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A Root Parameters to Represent an Array of Nameless Parameters.
 *
 * @since 6.2.0
 */
public class ArrayParameters<T extends AbstractParameters> extends AbstractParameters
        implements Iterable<T>, Serializable {

    /** @serial */
    private static final long serialVersionUID = 2058392199376865356L;

    public static final ParameterDefinition noname;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        noname = new ParameterDefinition("noname", ParameterValueType.PARAMETERS, true);

        parameterDefinitions = new ParameterDefinition[] {
            noname,
        };
    }

    private final Class<?> parametersClass;

    public ArrayParameters() {
        super(parameterDefinitions);
        this.parametersClass = resolveParametersClass(this.getClass());
    }

    public T[] getParametersArray() {
        return getParametersArray(noname);
    }

    public void addParameters(T parameters) {
        putValue(noname, parameters);
    }

    @Override
    public Iterator<T> iterator() {
        List<T> list = getParametersList(noname);
        if (list != null) {
            return list.iterator();
        } else {
            return Collections.emptyIterator();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public T newParameters(String name) {
        Parameter p = getParameter(name);
        if (p == null) {
            throw new UnknownParameterException(name, this);
        }
        try {
            T sub = (T)ClassUtils.createInstance(parametersClass);
            sub.setIdentifier(p);
            p.putValue(sub);
            return sub;
        } catch (Exception e) {
            throw new InvalidParameterValueException("Failed to instantiate " + parametersClass, e);
        }
    }

    private static Class<?> resolveParametersClass(Class<?> clazz) {
        Class<?> parametersClass = null;
        Type[] types = ((ParameterizedType)clazz.getGenericSuperclass()).getActualTypeArguments();
        if (types.length > 0) {
            parametersClass = (Class)types[0];
        }
        if (parametersClass == null) {
            return VariableParameters.class;
        } else {
            return parametersClass;
        }
    }

}
