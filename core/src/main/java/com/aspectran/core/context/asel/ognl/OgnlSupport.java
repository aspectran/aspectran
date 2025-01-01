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
package com.aspectran.core.context.asel.ognl;

import com.aspectran.utils.annotation.jsr305.NonNull;
import ognl.OgnlContext;

import java.util.Map;

/**
 * Support for expressions using OGNL.
 *
 * @since 6.0.0
 */
public abstract class OgnlSupport {

    public static final OgnlClassResolver CLASS_RESOLVER = new OgnlClassResolver();

    public static final OgnlMemberAccess MEMBER_ACCESS = new OgnlMemberAccess();

    @NonNull
    public static OgnlContext createDefaultContext() {
        return new OgnlContext(CLASS_RESOLVER, null, MEMBER_ACCESS);
    }

    @NonNull
    public static OgnlContext createDefaultContext(Map<String, Object> contextVariables) {
        OgnlContext ognlContext = createDefaultContext();
        if (contextVariables != null) {
            ognlContext.putAll(contextVariables);
        }
        return ognlContext;
    }

}
