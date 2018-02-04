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
package com.aspectran.shell.adapter;

import com.aspectran.core.adapter.BasicSessionAdapter;
import com.aspectran.core.component.session.SessionAgent;

/**
 * The Class ShellSessionAdapter.
 *
 * @since 2.3.0
 */
public class ShellSessionAdapter extends BasicSessionAdapter {

    /**
     * Instantiates a new ShellSessionAdapter.
     *
     * @param agent the session agent
     */
    public ShellSessionAdapter(SessionAgent agent) {
        super(agent);
    }

}
