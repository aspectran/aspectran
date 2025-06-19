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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.asel.token.Token;
import com.aspectran.core.context.asel.token.TokenParser;
import com.aspectran.core.context.env.Profiles;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.TextStyleType;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created: 2019/12/17</p>
 */
public class DescriptionRule implements Replicable<DescriptionRule> {

    private String profile;

    private Profiles profiles;

    private TextStyleType contentStyle;

    private String content;

    private String formattedContent;

    private List<DescriptionRule> candidates;

    public DescriptionRule() {
    }

    public DescriptionRule(DescriptionRule other) {
        if (other != null) {
            setProfiles(other.getProfile(), other.getProfiles());
            setContentStyle(other.getContentStyle());
            setContent(other.getContent());
            setFormattedContent(other.getFormattedContent());
            setCandidates(other.getCandidates());
        }
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
        this.profiles = (profile != null ? Profiles.of(profile) : null);
    }

    public Profiles getProfiles() {
        return profiles;
    }

    private void setProfiles(String profile, Profiles profiles) {
        this.profile = profile;
        this.profiles = profiles;
    }

    public TextStyleType getContentStyle() {
        return contentStyle;
    }

    public void setContentStyle(TextStyleType contentStyle) {
        this.contentStyle = contentStyle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFormattedContent() {
        return (formattedContent != null ? formattedContent : content);
    }

    public void setFormattedContent(String formattedContent) {
        this.formattedContent = formattedContent;
    }

    public List<DescriptionRule> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<DescriptionRule> candidates) {
        this.candidates = candidates;
    }

    public boolean addCandidate(DescriptionRule candidate) {
        if (candidates == null) {
            candidates = new ArrayList<>();
        }
        return candidates.add(candidate);
    }

    @Override
    public DescriptionRule replicate() {
        DescriptionRule dr = new DescriptionRule();
        dr.setProfiles(profile, profiles);
        dr.setContentStyle(contentStyle);
        dr.setContent(content);
        return dr;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("profile", profile);
        tsb.append("style", contentStyle);
        tsb.append("content", content);
        return tsb.toString();
    }

    public static String render(@NonNull DescriptionRule descriptionRule, Activity activity) {
        String content = descriptionRule.getFormattedContent();
        if (content == null || activity == null) {
            return content;
        }

        Token[] contentTokens = TokenParser.makeTokens(content, true);
        for (Token token : contentTokens) {
            Token.resolveValueProvider(token, activity.getClassLoader());
        }
        return activity.getTokenEvaluator().evaluateAsString(contentTokens);
    }

    @NonNull
    public static DescriptionRule newInstance(String profile, String style)
            throws IllegalRuleException {
        TextStyleType contentStyle = TextStyleType.resolve(style);
        if (style != null && contentStyle == null) {
            throw new IllegalRuleException("No text style type for '" + style + "'");
        }

        DescriptionRule dr = new DescriptionRule();
        dr.setContentStyle(contentStyle);
        if (StringUtils.hasText(profile)) {
            dr.setProfile(profile);
        }
        return dr;
    }

}
