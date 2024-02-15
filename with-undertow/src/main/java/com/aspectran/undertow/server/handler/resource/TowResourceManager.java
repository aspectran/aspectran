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
package com.aspectran.undertow.server.handler.resource;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.handlers.resource.PathResourceManager;

import java.io.File;
import java.io.IOException;

public class TowResourceManager extends PathResourceManager implements ApplicationAdapterAware {

    private ApplicationAdapter applicationAdapter;

    public TowResourceManager() {
        this(1024, true, false, (String[])null);
    }

    public TowResourceManager(File base) {
        this(base, 1024, true, false, (String[])null);
    }

    public TowResourceManager(File base, long transferMinSize) {
        this(base, transferMinSize, true, false, (String[])null);
    }

    public TowResourceManager(File base, long transferMinSize, boolean caseSensitive) {
        this(base, transferMinSize, caseSensitive, false, (String[])null);
    }

    public TowResourceManager(File base, long transferMinSize, boolean followLinks, String... safePaths) {
        this(base, transferMinSize, true, followLinks, safePaths);
    }

    protected TowResourceManager(long transferMinSize, boolean caseSensitive,
                                 boolean followLinks, String... safePaths) {
        super(transferMinSize, caseSensitive, followLinks, safePaths);
    }

    public TowResourceManager(@NonNull File base, long transferMinSize, boolean caseSensitive,
                              boolean followLinks, String... safePaths) {
        super(base.toPath(), transferMinSize, caseSensitive, followLinks, safePaths);
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public String getBase() {
        return super.getBasePath().toString();
    }

    public TowResourceManager setBase(String base) throws IOException {
        if (!StringUtils.hasText(base)) {
            throw new IllegalArgumentException("Resource base path must not be null or empty");
        }
        if (applicationAdapter != null) {
            return setBase(applicationAdapter.toRealPathAsFile(base));
        } else {
            return setBase(new File(base));
        }
    }

    @Override
    public TowResourceManager setBase(File base) {
        if (base == null) {
            throw new IllegalArgumentException("Resource base path must not be null");
        }
        if (!base.isDirectory()) {
            throw new IllegalArgumentException("Resource base path '" + base + "' does not exist");
        }
        super.setBase(base);
        return this;
    }

}
