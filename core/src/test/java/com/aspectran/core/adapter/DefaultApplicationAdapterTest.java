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
package com.aspectran.core.adapter;

import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2025. 1. 21.</p>
 */
class DefaultApplicationAdapterTest {

    @Test
    void testBasePath() throws IOException {
        File baseDir = ResourceUtils.getResourceAsFile(".");
        ApplicationAdapter applicationAdapter = new DefaultApplicationAdapter(baseDir.getPath());

        Path path1 = baseDir.toPath();
        Path path2 = applicationAdapter.getRealPath(".");
        assertEquals(path1, path2);

        Path path3 = applicationAdapter.getRealPath("sub-path1");
        Path path4 = applicationAdapter.getRealPath(applicationAdapter.getBasePath().resolve("sub-path1").toString());
        assertEquals(path3, path4);

        Path path5 = applicationAdapter.getRealPath("/aaa/bbb/../ccc/./123");
        Path path6 = Path.of(applicationAdapter.getBasePath().toString(), "/aaa/ccc/123");
        assertEquals(path5, path6);
    }

}
