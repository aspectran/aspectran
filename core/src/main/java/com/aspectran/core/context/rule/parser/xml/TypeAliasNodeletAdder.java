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
package com.aspectran.core.context.rule.parser.xml;

import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;
import org.jspecify.annotations.NonNull;

/**
 * A {@code NodeletAdder} for parsing the {@code <typeAlias>} elements within
 * the {@code <typeAliases>} section.
 *
 * <p>Created: 2016. 2. 13.</p>
 */
class TypeAliasNodeletAdder implements NodeletAdder {

    private static volatile TypeAliasNodeletAdder INSTANCE;

    static TypeAliasNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (TypeAliasNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TypeAliasNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("typeAliases")
            .child("typeAlias")
                .nodelet(attrs -> {
                    String alias = attrs.get("alias");
                    String type = attrs.get("type");
                    AspectranNodeParsingContext.getCurrentRuleParsingContext().addTypeAlias(alias, type);
                });
    }

}
