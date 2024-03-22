/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.context.resource;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ResourceManagerTest {

    @Test
    void checkResourceLocations() throws InvalidResourceException {
        String[] resourceLocations = new String[] {
                "/lib/ext",
                "/lib/ext/",
                "/lib/../lib/ext",
                "X:/lib/ext",
                "classpath:com/aspectran/core/context/resource",
                "classpath:com/aspectran/core/context/resource",
                "file:/C:/aspectran/file.jar",
                "file:/C:/aspectran/file.jar",
                "file://aspectran.com/file.jar",
                "file://aspectran.com/file.jar"
        };
        String[] result = ResourceManager.checkResourceLocations(resourceLocations, "/base");
        System.out.println(Arrays.toString(result));
    }

}
