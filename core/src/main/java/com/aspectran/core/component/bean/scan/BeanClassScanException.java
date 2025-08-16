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
package com.aspectran.core.component.bean.scan;

import com.aspectran.core.component.bean.BeanException;

import java.io.Serial;

/**
 * Runtime exception thrown when classpath scanning for beans fails.
 * Wraps underlying I/O or reflection issues encountered during scan.
 */
public class BeanClassScanException extends BeanException {

    @Serial
    private static final long serialVersionUID = -1301450076259511066L;

    public BeanClassScanException() {
        super();
    }

    public BeanClassScanException(String msg) {
        super(msg);
    }

    public BeanClassScanException(Throwable cause) {
        super(cause);
    }

    public BeanClassScanException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
