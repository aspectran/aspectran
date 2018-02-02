/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

/**
 * Supported Pointcut types.
 */
public enum PointcutType {

    WILDCARD("wildcard"),
    REGEXP("regexp"),
    SIMPLE_TRIGGER("simpleTrigger"),
    CRON_TRIGGER("cronTrigger");

    private final String alias;

    PointcutType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code PointcutType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the pointcut type as a {@code String}
     * @return a {@code PointcutType}, may be {@code null}
     */
    public static PointcutType resolve(String alias) {
        for (PointcutType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
