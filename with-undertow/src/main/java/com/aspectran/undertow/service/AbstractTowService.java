/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.undertow.service;

import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.CoreService;

/**
 * Abstract base class for {@code TowService} implementations.
 *
 * <p>Created: 2019-07-27</p>
 */
public abstract class AbstractTowService extends AspectranCoreService implements TowService {

    private String uriDecoding;

    public AbstractTowService() {
        super();
    }

    public AbstractTowService(CoreService rootService) {
        super(rootService);
    }

    public String getUriDecoding() {
        return uriDecoding;
    }

    protected void setUriDecoding(String uriDecoding) {
        this.uriDecoding = uriDecoding;
    }

}
