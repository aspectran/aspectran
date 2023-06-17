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
package com.aspectran.core.util;

import com.aspectran.core.context.rule.assistant.DefaultSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.aspectran.core.context.ActivityContext.ID_SEPARATOR;
import static com.aspectran.core.context.ActivityContext.ID_SEPARATOR_CHAR;
import static com.aspectran.core.context.ActivityContext.NAME_SEPARATOR;
import static com.aspectran.core.context.ActivityContext.NAME_SEPARATOR_CHAR;

/**
 * <p>Created: 2023/06/18</p>
 */
public class NamespaceUtils {

    public static String[] splitNamespace(String namespace) {
        if (!StringUtils.hasText(namespace)) {
            return null;
        }

        boolean absolutelyStart = namespace.startsWith(NAME_SEPARATOR);
        namespace = namespace.replace(NAME_SEPARATOR_CHAR, ID_SEPARATOR_CHAR);

        int cnt = StringUtils.search(namespace, ID_SEPARATOR_CHAR);
        if (cnt == 0) {
            if (absolutelyStart) {
                return new String[] { null, namespace };
            } else {
                return new String[] { namespace };
            }
        }

        List<String> list = new ArrayList<>();
        if (absolutelyStart) {
            list.add(null);
        }
        StringTokenizer st = new StringTokenizer(namespace, ID_SEPARATOR);
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list.toArray(new String[0]);
    }

    public static String applyNamespace(String[] nameArray, String lastName) {
        if (nameArray == null || nameArray.length == 0) {
            return lastName;
        }
        StringBuilder sb = new StringBuilder();
        for (String name : nameArray) {
            if (name != null && !name.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(ID_SEPARATOR);
                }
                sb.append(name);
            }
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(ID_SEPARATOR);
            }
            sb.append(lastName);
        }
        return sb.toString();
    }

    public static String applyNamespaceForTranslet(String[] nameArray, String lastName) {
        if (nameArray == null || nameArray.length == 0) {
            return lastName;
        }
        StringBuilder sb = new StringBuilder();
        for (String name : nameArray) {
            if (name != null && !name.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(NAME_SEPARATOR);
                }
                sb.append(name);
            }
        }
        if (lastName != null && !lastName.isEmpty()) {
            if (sb.length() > 0 && !lastName.startsWith(NAME_SEPARATOR)) {
                sb.append(NAME_SEPARATOR);
            }
            sb.append(lastName);
        }
        if (nameArray[0] == null && sb.charAt(0) != NAME_SEPARATOR_CHAR) {
            sb.insert(0, NAME_SEPARATOR);
        }
        return sb.toString();
    }

    /**
     * Returns the translet name of the prefix and suffix are combined.
     * @param transletName the translet name
     * @return the new translet name
     */
    public static String applyTransletNamePattern(DefaultSettings defaultSettings, String transletName) {
        return applyTransletNamePattern(defaultSettings, transletName, false);
    }

    /**
     * Returns the translet name of the prefix and suffix are combined.
     * @param transletName the translet name
     * @param absolutely whether to allow absolutely name for translet
     * @return the new translet name
     */
    public static String applyTransletNamePattern(DefaultSettings defaultSettings, String transletName, boolean absolutely) {
        String prefix = null;
        String suffix = null;
        if (defaultSettings != null) {
            prefix = defaultSettings.getTransletNamePrefix();
            suffix = defaultSettings.getTransletNameSuffix();
        }
        return applyTransletNamePattern(prefix, transletName, suffix, absolutely);
    }

    public static String applyTransletNamePattern(String prefix, String transletName, String suffix, boolean absolutely) {
        if (absolutely && StringUtils.startsWith(transletName, NAME_SEPARATOR_CHAR)) {
            return transletName;
        }
        if (prefix == null && suffix == null) {
            return transletName;
        }
        StringBuilder sb = new StringBuilder();
        if (prefix != null) {
            sb.append(prefix);
        }
        if (transletName != null) {
            sb.append(transletName);
        }
        if (suffix != null) {
            sb.append(suffix);
        }
        return sb.toString();
    }

}
