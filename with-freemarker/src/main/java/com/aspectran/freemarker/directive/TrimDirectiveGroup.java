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
package com.aspectran.freemarker.directive;

import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * A specialized map that groups {@link TrimDirective} instances by their group name.
 * <p>This class organizes directives into a nested map structure like {@code Map<GroupName, Map<DirectiveName, Directive>>}.
 * This structure is then used to register the groups as shared variables in the FreeMarker
 * {@link freemarker.template.Configuration}, allowing them to be used in templates with a
 * namespace, such as {@code <@sql.where>...</@sql.where>}.</p>
 *
 * @since 2016. 1. 31.
 */
public class TrimDirectiveGroup extends HashMap<String, Map<String, TrimDirective>> {

    @Serial
    private static final long serialVersionUID = 6709732757055800503L;

    /**
     * Constructs a new TrimDirectiveGroup and populates it with the given directives.
     * @param trimDirectives an array of {@link TrimDirective} instances to group
     */
    public TrimDirectiveGroup(TrimDirective @NonNull [] trimDirectives) {
        for (TrimDirective directive : trimDirectives) {
            putTrimDirective(directive);
        }
    }

    /**
     * Adds a single {@link TrimDirective} to the appropriate group.
     * If the group does not exist, it will be created.
     * @param trimDirective the directive to add
     * @return a map of directives belonging to the same group as the added directive
     */
    public Map<String, TrimDirective> putTrimDirective(@NonNull TrimDirective trimDirective) {
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
