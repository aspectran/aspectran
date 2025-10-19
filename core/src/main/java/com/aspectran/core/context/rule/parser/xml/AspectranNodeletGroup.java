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

import com.aspectran.utils.nodelet.NodeletGroup;

/**
 * The main {@link NodeletGroup} for parsing Aspectran's XML configuration.
 * <p>This class registers all the specific {@code NodeletAdder} implementations
 * that handle the various elements of the Aspectran DTD.</p>
 *
 * <p>Created: 2025-08-30</p>
 */
public class AspectranNodeletGroup extends NodeletGroup {

    private static volatile AspectranNodeletGroup INSTANCE;

    static AspectranNodeletGroup instance() {
        if (INSTANCE == null) {
            synchronized (AspectranNodeletGroup.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AspectranNodeletGroup();
                    INSTANCE.lazyInit();
                }
            }
        }
        return INSTANCE;
    }

    AspectranNodeletGroup() {
        super("aspectran");
    }

    private void lazyInit() {
        with(DiscriptionNodeletAdder.instance());
        with(SettingsNodeletAdder.instance());
        with(TypeAliasNodeletAdder.instance());
        with(AppendNodeletAdder.instance());
        with(EnvironmentNodeletAdder.instance());
        with(AspectNodeletAdder.instance());
        with(BeanNodeletAdder.instance());
        with(ScheduleNodeletAdder.instance());
        with(TemplateNodeletAdder.instance());
        with(TransletNodeletAdder.instance());
    }

}
