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

import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.nodelet.NodeletGroup;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class InnerBeanNodeletGroup extends NodeletGroup {

    private static volatile InnerBeanNodeletGroup INSTANCE;

    static InnerBeanNodeletGroup instance(int count) {
        if (INSTANCE == null) {
            synchronized (InnerBeanNodeletGroup.class) {
                if (INSTANCE == null) {
                    INSTANCE = new InnerBeanNodeletGroup();
                    INSTANCE.lazyInit();
                }
            }
        }
        System.out.println("InnerBeanNodeletGroup instance: " + count);
        return INSTANCE;
    }

    static final AtomicInteger count = new AtomicInteger(0);

    InnerBeanNodeletGroup() {
        super("bean");
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
        InnerBeanNodeletAdder.instance(0).addTo(this);
    }

}
