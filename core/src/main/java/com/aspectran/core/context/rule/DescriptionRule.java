/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.context.expr.TokenEvaluation;
import com.aspectran.core.context.expr.TokenEvaluator;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.ability.Replicable;
import com.aspectran.core.context.rule.type.TextStyleType;
import com.aspectran.core.util.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created: 2019/12/17</p>
 */
public class DescriptionRule implements Replicable<DescriptionRule> {

    private String profile;

    private TextStyleType contentStyle;

    private String content;

    private String formattedContent;

    private List<DescriptionRule> candidates;

    public DescriptionRule() {
    }

    public DescriptionRule(DescriptionRule dr) {
        if (dr != null) {
            setProfile(dr.getProfile());
            setContentStyle(dr.getContentStyle());
            setContent(dr.getContent());
            setFormattedContent(dr.getFormattedContent());
            setCandidates(dr.getCandidates());
        }
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
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

    public List<DescriptionRule> addCandidate(DescriptionRule descriptionRule) {
        if (candidates == null) {
            candidates = new ArrayList<>();
        }
        candidates.add(descriptionRule);
        return candidates;
    }

    @Override
    public DescriptionRule replicate() {
        DescriptionRule dr = new DescriptionRule();
        dr.setProfile(profile);
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

    public static String render(DescriptionRule descriptionRule, Activity activity) {
        String content = descriptionRule.getFormattedContent();
        if (content == null || activity == null) {
            return content;
        }

        Token[] contentTokens = TokenParser.makeTokens(content, true);
        for (Token token : contentTokens) {
            Token.resolveAlternativeValue(token, activity.getApplicationAdapter().getClassLoader());
        }
        TokenEvaluator evaluator = new TokenEvaluation(activity);
        return evaluator.evaluateAsString(contentTokens);
    }

    public static DescriptionRule newInstance(String profile, String style)
            throws IllegalRuleException {
        TextStyleType contentStyle = TextStyleType.resolve(style);
        if (style != null && contentStyle == null) {
            throw new IllegalRuleException("No text style type for '" + style + "'");
        }

        DescriptionRule descriptionRule = new DescriptionRule();
        descriptionRule.setContentStyle(contentStyle);
        if (profile != null && !profile.isEmpty()) {
            descriptionRule.setProfile(profile);
        }
        return descriptionRule;
    }

}
