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
package com.aspectran.core.context.rule.type;

import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Supported join point target types.
 * <dl>
 *     <dt>{@code activity}</dt>
 *     <dd>Activities are the target of the join points.</dd>
 *     <dt>{@code method}</dt>
 *     <dd>Target the Advisable methods in the Bean as a join points.
 *         The methods must be annotated with @Advisable.</dd>
 * </dl>
 */
public enum JoinpointTargetType {

    /**
     * Activities are the target of the join points.
     **/
    ACTIVITY("activity"),

    /**
     * Target the Advisable methods in the Bean as a join points.
     * The methods must be annotated with @Advisable.
     **/
    METHOD("method");

    private final String alias;

    JoinpointTargetType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code JoinpointTargetType} with a value represented
     * by the specified {@code String}.
     * @param alias the join-point target type as a {@code String}
     * @return a {@code JoinpointTargetType}, may be {@code null}
     */
    @Nullable
    public static JoinpointTargetType resolve(String alias) {
        for (JoinpointTargetType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
