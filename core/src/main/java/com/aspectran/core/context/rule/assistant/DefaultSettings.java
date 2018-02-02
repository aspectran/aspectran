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
package com.aspectran.core.context.rule.assistant;

import com.aspectran.core.activity.CoreTranslet;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.rule.type.DefaultSettingType;
import com.aspectran.core.util.BooleanUtils;
import com.aspectran.core.util.PrefixSuffixPattern;

import java.util.Map;

/**
 * The Class DefaultSettings
 * 
 * <p>Created: 2008. 03. 22 PM 5:48:09</p>
 */
public class DefaultSettings {

    private String transletNamePattern;

    private String transletNamePrefix;

    private String transletNameSuffix;

    private String transletInterfaceClassName;

    private Class<Translet> transletInterfaceClass;

    private String transletImplementationClassName;

    private Class<CoreTranslet> transletImplementationClass;

    private String beanProxifier;

    private Boolean pointcutPatternVerifiable;

    private String defaultTemplateEngineBean;

    private String defaultSchedulerBean;

    public DefaultSettings() {
    }

    public DefaultSettings(DefaultSettings ds) {
        this.transletNamePattern = ds.getTransletNamePattern();
        this.transletNamePrefix = ds.getTransletNamePrefix();
        this.transletNameSuffix = ds.getTransletNameSuffix();
        this.transletInterfaceClassName = ds.getTransletInterfaceClassName();
        this.transletInterfaceClass = ds.getTransletInterfaceClass();
        this.transletImplementationClassName = ds.getTransletImplementationClassName();
        this.transletImplementationClass = ds.getTransletImplementationClass();
        this.beanProxifier = ds.getBeanProxifier();
        this.pointcutPatternVerifiable = ds.getPointcutPatternVerifiable();
        this.defaultTemplateEngineBean = ds.getDefaultTemplateEngineBean();
        this.defaultSchedulerBean = ds.getDefaultSchedulerBean();
    }

    public String getTransletNamePattern() {
        return transletNamePattern;
    }

    public void setTransletNamePattern(String transletNamePattern) {
        this.transletNamePattern = transletNamePattern;

        if (transletNamePattern != null) {
            PrefixSuffixPattern prefixSuffixPattern = new PrefixSuffixPattern();

            if (prefixSuffixPattern.split(transletNamePattern)) {
                transletNamePrefix = prefixSuffixPattern.getPrefix();
                transletNameSuffix = prefixSuffixPattern.getSuffix();
            }
        }
    }

    public void setTransletNamePattern(String transletNamePrefix, String transletNameSuffix) {
        this.transletNamePattern = transletNamePrefix + PrefixSuffixPattern.PREFIX_SUFFIX_PATTERN_SEPARATOR + transletNameSuffix;
        this.transletNamePrefix = transletNamePrefix;
        this.transletNameSuffix = transletNameSuffix;
    }

    public void setTransletNamePrefix(String transletNamePrefix) {
        if (transletNameSuffix != null) {
            setTransletNamePattern(transletNamePrefix, transletNameSuffix);
        } else {
            this.transletNamePrefix = transletNamePrefix;
        }
    }

    public void setTransletNameSuffix(String transletNameSuffix) {
        if (transletNamePrefix != null) {
            setTransletNamePattern(transletNamePrefix, transletNameSuffix);
        } else {
            this.transletNameSuffix = transletNameSuffix;
        }
    }

    public String getTransletNamePrefix() {
        return transletNamePrefix;
    }

    public String getTransletNameSuffix() {
        return transletNameSuffix;
    }

    public String getTransletInterfaceClassName() {
        return transletInterfaceClassName;
    }

    public void setTransletInterfaceClassName(String transletInterfaceClassName) {
        this.transletInterfaceClassName = transletInterfaceClassName;
    }

    public Class<Translet> getTransletInterfaceClass() {
        return transletInterfaceClass;
    }

    public void setTransletInterfaceClass(Class<Translet> transletInterfaceClass) {
        this.transletInterfaceClass = transletInterfaceClass;
    }

    public String getTransletImplementationClassName() {
        return transletImplementationClassName;
    }

    public void setTransletImplementationClassName(String transletImplementationClassName) {
        this.transletImplementationClassName = transletImplementationClassName;
    }

    public Class<CoreTranslet> getTransletImplementationClass() {
        return transletImplementationClass;
    }

    public void setTransletImplementationClass(Class<CoreTranslet> transletImplementationClass) {
        this.transletImplementationClass = transletImplementationClass;
    }

    public String getBeanProxifier() {
        return beanProxifier;
    }

    public void setBeanProxifier(String beanProxifier) {
        this.beanProxifier = beanProxifier;
    }

    public boolean isPointcutPatternVerifiable() {
        return BooleanUtils.toBoolean(pointcutPatternVerifiable, true);
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

    public void apply(Map<DefaultSettingType, String> settings) throws ClassNotFoundException {
        if (settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN) != null) {
            setTransletNamePattern(settings.get(DefaultSettingType.TRANSLET_NAME_PATTERN));
        }
        if (settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX) != null) {
            setTransletNamePrefix(settings.get(DefaultSettingType.TRANSLET_NAME_PREFIX));
        }
        if (settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX) != null) {
            setTransletNameSuffix(settings.get(DefaultSettingType.TRANSLET_NAME_SUFFIX));
        }
        if (settings.get(DefaultSettingType.TRANSLET_INTERFACE_CLASS) != null) {
            setTransletInterfaceClassName(settings.get(DefaultSettingType.TRANSLET_INTERFACE_CLASS));
        }
        if (settings.get(DefaultSettingType.TRANSLET_IMPLEMENTATION_CLASS) != null) {
            setTransletImplementationClassName(settings.get(DefaultSettingType.TRANSLET_IMPLEMENTATION_CLASS));
        }
        if (settings.get(DefaultSettingType.BEAN_PROXIFIER) != null) {
            beanProxifier = settings.get(DefaultSettingType.BEAN_PROXIFIER);
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
