/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.component.session;

import com.aspectran.core.context.AspectranCheckedException;

/**
 * <p>Created: 2017. 9. 7.</p>
 */
public class UnreadableSessionDataException extends AspectranCheckedException {

    private static final long serialVersionUID = 799147544009142489L;

    private final String id;

    private final String groupName;

    public UnreadableSessionDataException(String id, String groupName, Throwable t) {
        super ("Unreadable session " + id + " for " + groupName, t);
        this.id = id;
        this.groupName = groupName;
    }

    public String getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

}

