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
package com.aspectran.core.activity.response.transform.xml;

import com.aspectran.core.activity.process.result.ProcessResult;
import org.xml.sax.InputSource;

/**
 * The Class ContentsInputSource.
 * 
 * <p>Created: 2008. 05. 26 PM 2:03:25</p>
 */
public class ContentsInputSource extends InputSource {

    private ProcessResult processResult;

    /**
     * Instantiates a new ContentsInputSource.
     *
     * @param processResult a {@code ProcessResult} object
     */
    public ContentsInputSource(ProcessResult processResult) {
        this.processResult = processResult;
    }

    /**
     * Returns a {@code ProcessResult} object.
     *
     * @return a {@code ProcessResult} object
     */
    public ProcessResult getProcessResult() {
        return processResult;
    }

}
