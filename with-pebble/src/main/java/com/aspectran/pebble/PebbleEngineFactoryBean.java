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
package com.aspectran.pebble;

import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import io.pebbletemplates.pebble.PebbleEngine;

/**
 * A {@link com.aspectran.core.component.bean.ablility.FactoryBean} that creates
 * a singleton {@link PebbleEngine} instance.
 *
 * <p>This class extends {@link PebbleEngineFactory} to provide a convenient way
 * to configure and instantiate a {@code PebbleEngine} as a singleton bean within
 * the Aspectran framework.</p>
 *
 * <p>Created: 2016. 1. 25.</p>
 */
public class PebbleEngineFactoryBean extends PebbleEngineFactory
        implements InitializableFactoryBean<PebbleEngine> {

    private PebbleEngine pebbleEngine;

    /**
     * Initializes the factory by creating the singleton {@link PebbleEngine} instance.
     */
    @Override
    public void initialize() {
        if (pebbleEngine == null) {
            pebbleEngine = createPebbleEngine();
        }
    }

    /**
     * Returns the singleton {@link PebbleEngine} instance.
     * @return the {@code PebbleEngine} instance
     */
    @Override
    public PebbleEngine getObject() {
        return pebbleEngine;
    }

}
