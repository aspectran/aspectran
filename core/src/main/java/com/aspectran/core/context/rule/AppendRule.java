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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.env.Profiles;
import com.aspectran.core.context.rule.params.AspectranParameters;
import com.aspectran.core.context.rule.type.AppendableFileFormatType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Defines a rule for importing an external configuration file (XML or APON)
 * into the current context, allowing for modular configuration.
 *
 * <p>Created: 2017. 05. 06.</p>
 */
public class AppendRule {

    private String file;

    private String resource;

    private String url;

    private AppendableFileFormatType format;

    private String profile;

    private Profiles profiles;

    private AspectranParameters aspectranParameters;

    /**
     * Gets the path to the local file to append.
     * @return the file path
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the path to the local file to append.
     * @param file the file path
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Gets the resource path on the classpath to append.
     * @return the resource path
     */
    public String getResource() {
        return resource;
    }

    /**
     * Sets the resource path on the classpath to append.
     * @param resource the resource path
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Gets the URL of the resource to append.
     * @return the URL string
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the resource to append.
     * @param url the URL string
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the format of the file to append (e.g., XML or APON).
     * @return the file format type
     */
    public AppendableFileFormatType getFormat() {
        return format;
    }

    /**
     * Sets the format of the file to append.
     * @param format the file format type
     */
    public void setFormat(AppendableFileFormatType format) {
        this.format = format;
    }

    /**
     * Gets the profile expression.
     * @return the profile expression
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Sets the profile expression that determines if this rule should be active.
     * @param profile the profile expression
     */
    public void setProfile(String profile) {
        this.profile = profile;
        this.profiles = (profile != null ? Profiles.of(profile) : null);
    }

    /**
     * Gets the parsed profiles.
     * @return the profiles
     */
    public Profiles getProfiles() {
        return profiles;
    }

    /**
     * Gets the Aspectran parameters to append.
     * @return the Aspectran parameters
     */
    public AspectranParameters getAspectranParameters() {
        return aspectranParameters;
    }

    /**
     * Sets the Aspectran parameters to append.
     * @param aspectranParameters the Aspectran parameters
     */
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
     * @param file the rule file to append
     * @param resource the rule resource to append
     * @param url the rule url to append
     * @param format the rule file type ('xml' or 'apon')
     * @param profile the environment profile name
     * @return an {@code AppendRule} object
     * @throws IllegalRuleException if an illegal rule is found
     */
    @NonNull
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
            throw new IllegalRuleException("The 'append' element requires either a 'file', 'resource' or 'url' attribute");
        }

        AppendableFileFormatType appendableFileFormatType = AppendableFileFormatType.resolve(format);
        if (format != null && appendableFileFormatType == null) {
            throw new IllegalRuleException("Not one of the appendable file formats: '" + format + "'");
        }
        appendRule.setFormat(appendableFileFormatType);

        if (StringUtils.hasText(profile)) {
            appendRule.setProfile(profile);
        }

        return appendRule;
    }

    /**
     * Create a new AppendRule.
     * @param aspectranParameters the aspectran parameters to append
     * @param profile the environment profile name
     * @return an {@code AppendRule} object
     */
    @NonNull
    public static AppendRule newInstance(AspectranParameters aspectranParameters, String profile) {
        AppendRule appendRule = new AppendRule();
        appendRule.setAspectranParameters(aspectranParameters);
        if (StringUtils.hasText(profile)) {
            appendRule.setProfile(profile);
        }
        return appendRule;
    }

}
