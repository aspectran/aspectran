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

import com.aspectran.core.context.rule.ChooseRule;
import com.aspectran.core.context.rule.ChooseWhenRule;
import com.aspectran.core.context.rule.ability.HasActionRules;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletGroup;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Class ChooseNodeParser.
 *
 * @since 6.0.0
 */
class ChooseNodeletGroup extends NodeletGroup {

    private static volatile ChooseNodeletGroup INSTANCE;

    static ChooseNodeletGroup instance(int count) {
        if (INSTANCE == null) {
            synchronized (ChooseNodeletGroup.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChooseNodeletGroup();
                    INSTANCE.lazyInit();
                }
            }
        }
        System.out.println("ChooseNodeletGroup instance: " + count);
        return INSTANCE;
    }

    private static final AtomicInteger count = new AtomicInteger(0);

    ChooseNodeletGroup() {
        super("choose");
    }

    @Override
    public NodeletGroup child(String name) {
        if (getName().equals(name)) {
            return this;
        } else  {
            return super.child(name);
        }
    }

    private void lazyInit() {
        ChooseNodeletAdder.instance(0).addTo(this);
    }

}
