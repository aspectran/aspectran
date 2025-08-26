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

import com.aspectran.utils.ToStringBuilder;
import freemarker.template.TemplateModelException;

import java.util.Map;

/**
 * A concrete implementation of {@link TrimDirective} that can be used in FreeMarker templates.
 * <p>This directive can be configured in two ways:
 * <ol>
 *   <li>With a pre-defined {@link Trimmer} instance, applying a fixed set of rules.</li>
 *   <li>Without a pre-defined trimmer, in which case it will parse parameters
 *       (e.g., {@code prefix}, {@code suffix}) directly from the template directive call.</li>
 * </ol>
 * This dual behavior allows for both reusable, pre-configured directives and flexible,
 * template-specific ones.</p>
 *
 * @since 2016. 1. 29.
 */
public class CustomTrimDirective extends AbstractTrimDirectiveModel implements TrimDirective {

    private final String groupName;

    private final String directiveName;

    private final Trimmer trimmer;

    /**
     * Instantiates a new CustomTrimDirective that will parse parameters from the template.
     * @param groupName the name of the directive group
     * @param directiveName the name of the directive
     */
    public CustomTrimDirective(String groupName, String directiveName) {
        this(groupName, directiveName, null);
    }

    /**
     * Instantiates a new CustomTrimDirective with a pre-configured trimmer.
     * @param groupName the name of the directive group
     * @param directiveName the name of the directive
     * @param trimmer the pre-configured {@link Trimmer} to use
     */
    public CustomTrimDirective(String groupName, String directiveName, Trimmer trimmer) {
        this.groupName = groupName;
        this.directiveName = directiveName;
        this.trimmer = trimmer;
    }

    /**
     * Instantiates a new CustomTrimDirective with explicit trimming rules.
     * @param groupName the name of the directive group
     * @param directiveName the name of the directive
     * @param prefix the prefix to add
     * @param suffix the suffix to add
     * @param deprefixes the prefixes to remove
     * @param desuffixes the suffixes to remove
     * @param caseSensitive whether matching is case-sensitive
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

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getDirectiveName() {
        return directiveName;
    }

    /**
     * Returns a {@link Trimmer} instance to be used for processing.
     * <p>If this directive was created with a pre-configured trimmer, that instance is returned.
     * Otherwise, it creates a new {@code Trimmer} on-the-fly using the parameters
     * passed to the directive in the template.</p>
     * @param params the parameters passed from the template directive call
     * @return a configured {@code Trimmer}
     * @throws TemplateModelException if a parameter is invalid
     */
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
