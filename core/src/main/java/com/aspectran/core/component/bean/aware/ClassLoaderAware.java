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
package com.aspectran.core.component.bean.aware;

/**
 * The ClassLoaderAware interface provides the ability to configure a {@code ClassLoader}
 * to be used by the implementing object when loading classes or resources.
 * 
 * <p>Created: 2016. 1. 25.</p>
 *
 * @since 2.0.0
 */
public interface ClassLoaderAware extends Aware {

    /**
     * Specify the {@code ClassLoader} to provide. The {@code ClassLoader} can be set
     * when the object is created, and allows the creator to provide the appropriate class
     * loader to be used by the object when when loading classes and resources.
     *
     * @param classLoader the {@code ClassLoader} to provide
     */
    void setClassLoader(ClassLoader classLoader);

}
