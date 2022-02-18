/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.activity.request;

import java.util.LinkedHashMap;

/**
 * The Class FileParameterMap.
 * 
 * <p>Created: 2008. 03. 29 PM 6:23:00</p>
 */
public class FileParameterMap extends LinkedHashMap<String, FileParameter[]> {

    private static final long serialVersionUID = -2589963778315184242L;

    public FileParameter getFileParameter(String name) {
        FileParameter[] fileParameters = get(name);
        return (fileParameters != null && fileParameters.length > 0 ? fileParameters[0] : null);
    }

    public FileParameter[] getFileParameterValues(String name) {
        return get(name);
    }

    public void setFileParameter(String name, FileParameter fileParameter) {
        put(name, new FileParameter[] { fileParameter });
    }

    public void setFileParameterValues(String name, FileParameter[] fileParameters) {
        put(name, fileParameters);
    }

}
