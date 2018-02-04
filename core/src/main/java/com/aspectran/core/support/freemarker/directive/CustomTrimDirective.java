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

import com.aspectran.core.util.ToStringBuilder;
import freemarker.template.TemplateModelException;

import java.util.Map;

/**
 * The Class CustomTrimDirective.
 *
 * <p>Created: 2016. 1. 29.</p>
 */
public class CustomTrimDirective extends AbstractTrimDirectiveModel implements TrimDirective {

    private final String groupName;

    private final String directiveName;

    private final Trimmer trimmer;

    /**
     * Instantiates a new Custom trim directive.
     *
     * @param groupName the group name
     * @param directiveName the directive name
     */
    public CustomTrimDirective(String groupName, String directiveName) {
        this(groupName, directiveName, null);
    }

    /**
     * Instantiates a new Custom trim directive.
     *
     * @param groupName the group name
     * @param directiveName the directive name
     * @param trimmer the trimmer
     */
    public CustomTrimDirective(String groupName, String directiveName, Trimmer trimmer) {
        this.groupName = groupName;
        this.directiveName = directiveName;
        this.trimmer = trimmer;
    }

    /**
     * Instantiates a new Custom trim directive.
     *
     * @param groupName the group name
     * @param directiveName the directive name
     * @param prefix the prefix
     * @param suffix the suffix
     * @param deprefixes the prefixes to be removed from the leading of body string
     * @param desuffixes the suffixes to be removed from the tailing of body string
     * @param caseSensitive true to case sensitive; false to ignore case sensitive
     */
    public CustomTrimDirective(String groupName, String directiveName, String prefix, String suffix,
                               String[] deprefixes, String[] desuffixes, boolean caseSensitive) {
        this.groupName = groupName;
        this.directiveName = directiveName;

        Trimmer trimmer = new Trimmer();
        trimmer.setPrefix(prefix);
        trimmer.setSuffix(suffix);
        trimmer.setDeprefixes(deprefixes);
        trimmer.setDesuffixes(desuffixes);
        trimmer.setCaseSensitive(caseSensitive);
        this.trimmer = trimmer;
    }

    /**
     * Gets group name.
     *
     * @return the group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Gets directive name.
     *
     * @return the directive name
     */
    public String getDirectiveName() {
        return directiveName;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Trimmer getTrimmer(Map params) throws TemplateModelException {
        if (this.trimmer == null) {
            String prefix = parseStringParameter(params, TrimDirective.PREFIX_PARAM_NAME);
            String suffix = parseStringParameter(params, TrimDirective.SUFFIX_PARAM_NAME);
            String[] deprefixes = parseSequenceParameter(params, TrimDirective.DEPREFIXES_PARAM_NAME);
            String[] desuffixes = parseSequenceParameter(params, TrimDirective.DESUFFIXES_PARAM_NAME);
            String caseSensitive = parseStringParameter(params, TrimDirective.CASE_SENSITIVE_PARAM_NAME);

            Trimmer trimmer = new Trimmer();
            trimmer.setPrefix(prefix);
            trimmer.setSuffix(suffix);
            trimmer.setDeprefixes(deprefixes);
            trimmer.setDesuffixes(desuffixes);
            trimmer.setCaseSensitive(Boolean.parseBoolean(caseSensitive));
            return trimmer;
        } else {
            return this.trimmer;
        }
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("groupName", groupName);
        tsb.append("directiveName", directiveName);
        tsb.append("trimmer", trimmer);
        return tsb.toString();
    }

}
