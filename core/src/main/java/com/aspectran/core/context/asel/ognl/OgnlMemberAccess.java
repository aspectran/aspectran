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
package com.aspectran.core.context.asel.ognl;

import com.aspectran.utils.annotation.jsr305.NonNull;
import ognl.MemberAccess;
import ognl.OgnlContext;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A custom OGNL {@link MemberAccess} implementation that enforces security restrictions
 * on member access during expression evaluation.
 * <p>This class prevents OGNL expressions from accessing potentially dangerous or
 * unauthorized classes and methods by consulting the rules defined in {@link OgnlRestrictions}.
 * It ensures that only public members of allowed types can be accessed.</p>
 */
public class OgnlMemberAccess implements MemberAccess {

    @Override
    public Object setup(OgnlContext context, Object target, Member member, String propertyName) {
        return null;
    }

    @Override
    public void restore(OgnlContext context, Object target, Member member, String propertyName, Object state) {
    }

    @Override
    public boolean isAccessible(OgnlContext context, Object target, @NonNull Member member, String propertyName) {
        int modifiers = member.getModifiers();
        if (!Modifier.isPublic(modifiers)) {
            return false;
        }
        if (member instanceof Method) {
            if (!OgnlRestrictions.isMemberAllowed(target, member.getName())) {
                throw new OgnlRestrictionException(
                    String.format(
                        "Accessing member '%s' is forbidden for type '%s' in this expression context.",
                        member.getName(), target.getClass()));
            }
        }
        return true;
    }

}
