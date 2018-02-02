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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.AppenderFileFormatType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.ToStringBuilder;

/**
 * The Class AppendRule.
 * 
 * <p>Created: 2017. 05. 06.</p>
 */
public class AppendRule {

    private String file;

    private String resource;

    private String url;

    private String format;

    private String profile;

    private AspectranParameters aspectranParameters;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public AspectranParameters getAspectranParameters() {
        return aspectranParameters;
    }

    public void setAspectranParameters(AspectranParameters aspectranParameters) {
        this.aspectranParameters = aspectranParameters;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("file", file);
        tsb.append("resource", resource);
        tsb.append("url", url);
        tsb.append("format", format);
        tsb.append("profile", profile);
        tsb.append("aspectran", aspectranParameters);
        return tsb.toString();
    }

    /**
     * Create a new AppendRule.
     *
     * @param file the rule file to append
     * @param resource the rule resource to append
     * @param url the rule url to append
     * @param format the rule file type ('xml' or 'apon')
     * @param profile the environment profile name
     * @return an {@code AppendRule} object
     * @throws IllegalRuleException if an illegal rule is found
     */
    public static AppendRule newInstance(String file, String resource, String url, String format, String profile)
            throws IllegalRuleException {
        AppendRule appendRule = new AppendRule();

        if (StringUtils.hasText(file)) {
            appendRule.setFile(file);
        } else if (StringUtils.hasText(resource)) {
            appendRule.setResource(resource);
        } else if (StringUtils.hasText(url)) {
            appendRule.setUrl(url);
        } else {
            throw new IllegalRuleException("The 'append' element requires either a 'file' or a 'resource' or a 'url' attribute");
        }

        AppenderFileFormatType appenderFileFormatType = AppenderFileFormatType.resolve(format);
        if (format != null && appenderFileFormatType == null) {
            throw new IllegalRuleException("No appender file format type for '" + format + "'");
        }

        if (profile != null && !profile.isEmpty()) {
            appendRule.setProfile(profile);
        }

        return appendRule;
    }

    /**
     * Create a new AppendRule.
     *
     * @param aspectran the sub aspectran to append
     * @param profile the environment profile name
     * @return an {@code AppendRule} object
     */
    public static AppendRule newInstance(AspectranParameters aspectran, String profile) {
        AppendRule appendRule = new AppendRule();
        appendRule.setAspectranParameters(aspectran);
        if (profile != null && !profile.isEmpty()) {
            appendRule.setProfile(profile);
        }
        return appendRule;
    }

}
