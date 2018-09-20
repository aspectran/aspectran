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
package com.aspectran.core.adapter;

/**
 * The Class BasicRequestAdapter.
  *
 * @since 2016. 2. 13.
*/
public class BasicRequestAdapter extends AbstractRequestAdapter {

    /**
     * Instantiates a new BasicRequestAdapter.
     *
     * @param adaptee the adaptee object
     */
    public BasicRequestAdapter(Object adaptee) {
        super(adaptee, false);
    }

}
