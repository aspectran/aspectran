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
 * Supported auto-reload types.
 * 
 * <p>Created: 2019. 03. 19.</p>
 */
public enum AutoReloadType {

    HARD("hard"),
    SOFT("soft");

    private final String alias;

    AutoReloadType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns an {@code AutoReloadType} with a value represented
     * by the specified {@code String}.
     * @param alias the auto reload type as a {@code String}
     * @return an {@code AutoReloadType}, may be {@code null}
     */
    @Nullable
    public static AutoReloadType resolve(String alias) {
        for (AutoReloadType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
