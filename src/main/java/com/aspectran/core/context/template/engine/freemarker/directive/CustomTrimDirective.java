/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.template.engine.freemarker.directive;

import freemarker.template.TemplateModelException;

import java.util.Map;

/**
 * The Class CustomTrimDirective.
 *
 * <p>Created: 2016. 1. 29.</p>
 */
public class CustomTrimDirective extends AbstractTrimDirectiveModel {

    private final Trimmer trimmer;

    public CustomTrimDirective(String directiveName, Trimmer trimmer) {
        super(directiveName);
        this.trimmer = trimmer;
    }

    /**
     * Instantiates a new Custom trim directive.
     *
     * @param directiveName the directive name
     * @param prefix the prefix
     * @param suffix the suffix
     * @param deprefixes the prefixes to be removed from the leading of body string.
     * @param desuffixes the suffixes to be removed from the tailing of body string.
     * @param caseSensitive true to case sensitive; false to ignore case sensitive
     */
    public CustomTrimDirective(String directiveName, String prefix, String suffix, String[] deprefixes, String[] desuffixes, boolean caseSensitive) {
        super(directiveName);

        Trimmer trimmer = new Trimmer();
        trimmer.setPrefix(prefix);
        trimmer.setSuffix(suffix);
        trimmer.setDeprefixes(deprefixes);
        trimmer.setDesuffixes(desuffixes);
        trimmer.setCaseSensitive(caseSensitive);
        this.trimmer = trimmer;
    }

    @Override
    protected Trimmer getTrimmer(Map params) throws TemplateModelException {
        return trimmer;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{name=").append(getDirectiveName());
        sb.append(", trimmer=").append(trimmer);
        sb.append("}");

        return sb.toString();
    }

}
