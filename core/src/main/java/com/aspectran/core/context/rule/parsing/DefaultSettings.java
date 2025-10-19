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
package com.aspectran.core.context.rule.parsing;

import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.utils.BooleanUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.Map;

/**
 * Manages default settings that influence the behavior of the rule parsing process.
 *
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class DefaultSettings {

    /** Prefix to append to each translet name */
    private String transletNamePrefix;

    /** Suffix to append to each translet name */
    private String transletNameSuffix;

    private Boolean pointcutPatternVerifiable;

    private String defaultTemplateEngineBean;

    private String defaultSchedulerBean;

    public DefaultSettings() {
    }

    public DefaultSettings(@NonNull DefaultSettings ds) {
        this.transletNamePrefix = ds.getTransletNamePrefix();
        this.transletNameSuffix = ds.getTransletNameSuffix();
        this.pointcutPatternVerifiable = ds.getPointcutPatternVerifiable();
        this.defaultTemplateEngineBean = ds.getDefaultTemplateEngineBean();
        this.defaultSchedulerBean = ds.getDefaultSchedulerBean();
    }

    public String getTransletNamePrefix() {
        return transletNamePrefix;
    }

    public void setTransletNamePrefix(String transletNamePrefix) {
        this.transletNamePrefix = transletNamePrefix;
    }

    public String getTransletNameSuffix() {
        return transletNameSuffix;
    }

    public void setTransletNameSuffix(String transletNameSuffix) {
        this.transletNameSuffix = transletNameSuffix;
    }

    public boolean isPointcutPatternVerifiable() {
        return BooleanUtils.toBoolean(pointcutPatternVerifiable, false);
    }

    public Boolean getPointcutPatternVerifiable() {
        return pointcutPatternVerifiable;
    }

    public void setPointcutPatternVerifiable(boolean pointcutPatternVerifiable) {
        this.pointcutPatternVerifiable = pointcutPatternVerifiable;
    }

    public String getDefaultTemplateEngineBean() {
        return defaultTemplateEngineBean;
    }

    public void setDefaultTemplateEngineBean(String defaultTemplateEngineBean) {
        this.defaultTemplateEngineBean = defaultTemplateEngineBean;
    }

    public String getDefaultSchedulerBean() {
        return defaultSchedulerBean;
    }

    public void setDefaultSchedulerBean(String defaultSchedulerBean) {
        this.defaultSchedulerBean = defaultSchedulerBean;
    }

    public void apply(@NonNull Map<DefaultSettingType, String> settings) {
        if (settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX) != null) {
            setTransletNamePrefix(settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX));
        }
        if (settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX) != null) {
            setTransletNameSuffix(settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX));
        }
        if (settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE) != null) {
            pointcutPatternVerifiable = (settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE) == null
                    || Boolean.parseBoolean(settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE)));
        }
        if (settings.get(DefaultSettingType.DEFAULT_TEMPLATE_ENGINE_BEAN) != null) {
            defaultTemplateEngineBean = settings.get(DefaultSettingType.DEFAULT_TEMPLATE_ENGINE_BEAN);
        }
        if (settings.get(DefaultSettingType.DEFAULT_SCHEDULER_BEAN) != null) {
            defaultSchedulerBean = settings.get(DefaultSettingType.DEFAULT_SCHEDULER_BEAN);
        }
    }

}
