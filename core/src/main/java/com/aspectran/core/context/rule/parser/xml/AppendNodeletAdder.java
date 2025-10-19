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

import com.aspectran.core.context.rule.AppendRule;
import com.aspectran.core.context.rule.appender.RuleAppendHandler;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.nodelet.NodeletAdder;
import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * A {@code NodeletAdder} for parsing the {@code <append>} element, which is used
 * to include other configuration files.
 *
 * @see com.aspectran.core.context.rule.AppendRule
 * <p>Created: 2017. 4. 24.</p>
 */
class AppendNodeletAdder implements NodeletAdder {

    private static volatile AppendNodeletAdder INSTANCE;

    static AppendNodeletAdder instance() {
        if (INSTANCE == null) {
            synchronized (AppendNodeletAdder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppendNodeletAdder();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void addTo(@NonNull NodeletGroup group) {
        group.child("append")
            .nodelet(attrs -> {
                String file = attrs.get("file");
                String resource = attrs.get("resource");
                String url = attrs.get("url");
                String format = attrs.get("format");
                String profile = attrs.get("profile");

                RuleAppendHandler appendHandler = AspectranNodeParsingContext.getCurrentRuleParsingContext().getRuleAppendHandler();
                if (appendHandler != null) {
                    AppendRule appendRule = AppendRule.newInstance(file, resource, url, format, profile);
                    appendHandler.pending(appendRule);
                }
            });
    }

}
