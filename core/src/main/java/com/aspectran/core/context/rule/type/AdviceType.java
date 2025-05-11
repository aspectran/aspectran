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
package com.aspectran.core.context.rule.type;

import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Advice types include "around", "before" and "after" advice.
 * <dl>
 * <dt>Before advice: <dd>Advice that executes before a join point.
 * <dt>After advice: <dd>Advice to be executed after a join point completes normally.
 * <dt>Finally advice: <dd>Advice to be executed regardless of the means by which a join point exits (normal or exceptional return).
 * <dt>Around advice: <dd>Before advice + After advice
 * </dl>
 */
public enum AdviceType {

    BEFORE("before"),
    AFTER("after"),
    AROUND("around"),
    THROWN("thrown"),
    FINALLY("finally");

    private final String alias;

    AdviceType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns an {@code AdviceType} with a value represented
     * by the specified {@code String}.
     * @param alias the advice type as a {@code String}
     * @return an {@code AdviceType}, may be {@code null}
     */
    @Nullable
    public static AdviceType resolve(String alias) {
        for (AdviceType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
