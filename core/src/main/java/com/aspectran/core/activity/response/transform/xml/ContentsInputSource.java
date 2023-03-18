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
package com.aspectran.core.activity.response.transform.xml;

import org.xml.sax.InputSource;

/**
 * The Class ContentsInputSource.
 * 
 * <p>Created: 2008. 05. 26 PM 2:03:25</p>
 */
public class ContentsInputSource extends InputSource {

    private final Object data;

    /**
     * Instantiates a new ContentsInputSource.
     * @param data the data to be the input source
     */
    public ContentsInputSource(Object data) {
        this.data = data;
    }

    /**
     * Returns the data to be the input source.
     * @return the data to be the input source
     */
    public Object getData() {
        return data;
    }

}
