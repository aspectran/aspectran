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
 * <p>Created: 2008. 06. 14 AM 6:56:29</p>
 */
class ItemNodeletGroup extends NodeletGroup {

    private static volatile ItemNodeletGroup INSTANCE;

    static ItemNodeletGroup instance() {
        if (INSTANCE == null) {
            synchronized (ItemNodeletGroup.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ItemNodeletGroup();
                    INSTANCE.lazyInit();
                }
            }
        }
        return INSTANCE;
    }

    ItemNodeletGroup() {
        super("item", true);
    }

    private void lazyInit() {
        ItemNodeletAdder.instance().addTo(this);
    }

}
