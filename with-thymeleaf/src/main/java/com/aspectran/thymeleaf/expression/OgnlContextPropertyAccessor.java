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
package com.aspectran.thymeleaf.expression;

import com.aspectran.utils.annotation.jsr305.NonNull;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.PropertyAccessor;
import ognl.enhance.UnsupportedCompilationException;
import org.thymeleaf.context.IContext;

/**
 * <p>Created: 2024. 11. 25.</p>
 */
final class OgnlContextPropertyAccessor implements PropertyAccessor {

    public static final String RESTRICT_REQUEST_PARAMETERS = "%RESTRICT_REQUEST_PARAMETERS%";

    static final String REQUEST_PARAMETERS_RESTRICTED_VARIABLE_NAME = "param";

    @Override
    public Object getProperty(OgnlContext ognlContext, Object target, Object name) throws OgnlException {
        if (!(target instanceof IContext context)) {
            throw new IllegalStateException(
                    "Wrong target type. This property accessor is only usable for " + IContext.class.getName() + " implementations, and " +
                    "in this case the target object is " + (target == null? "null" : ("of class " + target.getClass().getName())));
        }

        if (REQUEST_PARAMETERS_RESTRICTED_VARIABLE_NAME.equals(name) &&
                ognlContext != null && ognlContext.containsKey(RESTRICT_REQUEST_PARAMETERS)) {
            throw new OgnlException(
                    "Access to variable \"" + name + "\" is forbidden in this context. Note some restrictions apply to " +
                    "variable access. For example, direct access to request parameters is forbidden in preprocessing and " +
                    "unescaped expressions, in TEXT template mode, in fragment insertion specifications and " +
                    "in some specific attribute processors.");
        }

        String propertyName = (name == null? null : name.toString());

        /*
         * NOTE we do not check here whether we are being asked for the 'locale', 'request', 'response', etc.
         * because there already are specific expression objects for the most important of them, which should
         * be used instead: #locale, #httpServletRequest, #httpSession, etc.
         * The variables maps should just be used as a map, without exposure of its more-internal methods...
         */
        return context.getVariable(propertyName);
    }

    @Override
    public void setProperty(OgnlContext context, Object target, Object name, Object value) throws OgnlException {
        // IVariablesMap implementations should never be set values from OGNL expressions
        throw new UnsupportedOperationException("Cannot set values into VariablesMap instances from OGNL Expressions");
    }

    @Override
    @NonNull
    public String getSourceAccessor(@NonNull OgnlContext context, Object target, Object index) {
        // This method is called during OGNL's bytecode enhancement optimizations in order to determine better-
        // performing methods to access the properties of an object. It's been written trying to mimic
        // what is done at MapPropertyAccessor#getSourceAccessor() method, removing all the parts related to indexed
        // access, which do not apply to IVariablesMap implementations.
        context.setCurrentAccessor(IContext.class);
        context.setCurrentType(Object.class);
        return ".getVariable(" + index + ")";
    }

    @Override
    public String getSourceSetter(@NonNull OgnlContext context, Object target, Object index) {
        // This method is called during OGNL's bytecode enhancement optimizations in order to determine better-
        // performing methods to access the properties of an object. Given IVariablesMap implementations should never
        // be set any values from OGNL, this exception should never be thrown anyway.
        throw new UnsupportedCompilationException(
                "Setting expression for " + context.getCurrentObject() + " with index of " + index + " cannot " +
                "be computed. IVariablesMap implementations are considered read-only by OGNL.");
    }

}
