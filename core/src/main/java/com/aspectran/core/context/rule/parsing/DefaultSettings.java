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
import org.jspecify.annotations.NonNull;

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

    /** Whether pointcut patterns should be verified */
    private Boolean pointcutPatternVerifiable;

    /** ID of the default template engine bean */
    private String defaultTemplateEngineBean;

    /** ID of the default scheduler bean */
    private String defaultSchedulerBean;

    /**
     * Constructs a new DefaultSettings instance.
     */
    public DefaultSettings() {
    }

    /**
     * Constructs a new DefaultSettings instance by copying settings from another DefaultSettings instance.
     * @param ds the default settings to copy from
     */
    public DefaultSettings(@NonNull DefaultSettings ds) {
        this.transletNamePrefix = ds.getTransletNamePrefix();
        this.transletNameSuffix = ds.getTransletNameSuffix();
        this.pointcutPatternVerifiable = ds.getPointcutPatternVerifiable();
        this.defaultTemplateEngineBean = ds.getDefaultTemplateEngineBean();
        this.defaultSchedulerBean = ds.getDefaultSchedulerBean();
    }

    /**
     * Returns the prefix to append to each translet name.
     * @return the translet name prefix
     */
    public String getTransletNamePrefix() {
        return transletNamePrefix;
    }

    /**
     * Sets the prefix to append to each translet name.
     * @param transletNamePrefix the translet name prefix to set
     */
    public void setTransletNamePrefix(String transletNamePrefix) {
        this.transletNamePrefix = transletNamePrefix;
    }

    /**
     * Returns the suffix to append to each translet name.
     * @return the translet name suffix
     */
    public String getTransletNameSuffix() {
        return transletNameSuffix;
    }

    /**
     * Sets the suffix to append to each translet name.
     * @param transletNameSuffix the translet name suffix to set
     */
    public void setTransletNameSuffix(String transletNameSuffix) {
        this.transletNameSuffix = transletNameSuffix;
    }

    /**
     * Returns whether pointcut patterns should be verified.
     * @return true if pointcut patterns should be verified; false otherwise
     */
    public boolean isPointcutPatternVerifiable() {
        return BooleanUtils.toBoolean(pointcutPatternVerifiable, false);
    }

    /**
     * Returns the raw Boolean value indicating whether pointcut patterns should be verified.
     * @return the Boolean value for pointcut pattern verifiability, or null if unset
     */
    public Boolean getPointcutPatternVerifiable() {
        return pointcutPatternVerifiable;
    }

    /**
     * Sets whether pointcut patterns should be verified.
     * @param pointcutPatternVerifiable true to verify pointcut patterns; false otherwise
     */
    public void setPointcutPatternVerifiable(boolean pointcutPatternVerifiable) {
        this.pointcutPatternVerifiable = pointcutPatternVerifiable;
    }

    /**
     * Returns the bean ID of the default template engine.
     * @return the default template engine bean ID
     */
    public String getDefaultTemplateEngineBean() {
        return defaultTemplateEngineBean;
    }

    /**
     * Sets the bean ID of the default template engine.
     * @param defaultTemplateEngineBean the default template engine bean ID to set
     */
    public void setDefaultTemplateEngineBean(String defaultTemplateEngineBean) {
        this.defaultTemplateEngineBean = defaultTemplateEngineBean;
    }

    /**
     * Returns the bean ID of the default scheduler.
     * @return the default scheduler bean ID
     */
    public String getDefaultSchedulerBean() {
        return defaultSchedulerBean;
    }

    /**
     * Sets the bean ID of the default scheduler.
     * @param defaultSchedulerBean the default scheduler bean ID to set
     */
    public void setDefaultSchedulerBean(String defaultSchedulerBean) {
        this.defaultSchedulerBean = defaultSchedulerBean;
    }

    /**
     * Applies default settings from the specified settings map.
     * @param settings a map containing default setting types and their string values
     */
    public void apply(@NonNull Map<DefaultSettingType, String> settings) {
        if (settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX) != null) {
            setTransletNamePrefix(settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX));
        }
        if (settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX) != null) {
            setTransletNameSuffix(settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX));
        }
        if (settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE) != null) {
            pointcutPatternVerifiable = Boolean.parseBoolean(settings.get(DefaultSettingType.POINTCUT_PATTERN_VERIFIABLE));
        }
        if (settings.get(DefaultSettingType.DEFAULT_TEMPLATE_ENGINE_BEAN) != null) {
            defaultTemplateEngineBean = settings.get(DefaultSettingType.DEFAULT_TEMPLATE_ENGINE_BEAN);
        }
        if (settings.get(DefaultSettingType.DEFAULT_SCHEDULER_BEAN) != null) {
            defaultSchedulerBean = settings.get(DefaultSettingType.DEFAULT_SCHEDULER_BEAN);
        }
    }

}
