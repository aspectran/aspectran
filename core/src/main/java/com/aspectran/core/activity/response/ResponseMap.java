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
package com.aspectran.core.activity.response;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * The Class ResponseMap.
 * 
 * <p>Created: 2008. 03. 29 PM 11:50:02</p>
 */
public class ResponseMap extends LinkedHashMap<String, Response> implements Iterable<Response> {

    /** @serial */
    private static final long serialVersionUID = 2093093144584776388L;

    public Response getFirst() {
        if (size() > 0) {
            return (Response)values().toArray()[0];
        } else {
            return null;
        }
    }

    @Override
    public Iterator<Response> iterator() {
        return this.values().iterator();
    }

}
