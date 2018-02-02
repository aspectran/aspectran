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
package com.aspectran.core.support.freemarker.directive;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class TrimDirectiveGroup.
 *
 * <p>Created: 2016. 1. 31.</p>
 */
public class TrimDirectiveGroup extends HashMap<String, Map<String, TrimDirective>> {

    /** @serial */
    private static final long serialVersionUID = 6709732757055800503L;

    public TrimDirectiveGroup(TrimDirective[] trimDirectives) {
        for (TrimDirective directive : trimDirectives) {
            putTrimDirective(directive);
        }
    }

    public Map<String, TrimDirective> putTrimDirective(TrimDirective trimDirective) {
        String groupName = trimDirective.getGroupName();
        Map<String, TrimDirective> directives = get(groupName);

        if (directives != null) {
            directives.put(trimDirective.getDirectiveName(), trimDirective);
        } else {
            directives = new HashMap<>();
            directives.put(trimDirective.getDirectiveName(), trimDirective);
            put(trimDirective.getGroupName(), directives);
        }

        return directives;
    }

}
