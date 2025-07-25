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
package com.aspectran.core.support;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityData;
import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.aware.CurrentActivityAware;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * {@link CurrentActivityDataFactoryBean} that returns the {@link ActivityData} for
 * the current request.
 * It should be declared as a {@code request} or {@code prototype} bean because it is
 * intended to use the value that the current Translet has.
 *
 * <p>Created: 2017. 10. 24.</p>
 */
public class CurrentActivityDataFactoryBean implements CurrentActivityAware, FactoryBean<ActivityData> {

    private String attributeName;

    private Activity activity;

    /**
     * Returns whether the current {@code ActivityData} is registered as an attribute
     * in the request scope.
     * @return true if the current {@code ActivityData} is registered as an attribute
     *      in the request scope; otherwise false
     */
    public boolean isAttributable() {
        return (attributeName != null);
    }

    /**
     * Returns the attribute name of the current {@code ActivityData} specified to register
     * in the request scope.
     * @return the attribute name
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * Specifies the attribute name for registering the current {@code ActivityData} as an
     * attribute in the request scope.
     * @param attributeName the attribute name of the current {@code ActivityData} to be
     *      registered in the request scope.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public void setCurrentActivity(@NonNull Activity activity) {
        Assert.state(this.activity == null, "Current activity is already set");
        this.activity = activity;
        if (activity.hasTranslet() && attributeName != null) {
            activity.getTranslet().setAttribute(attributeName, activity.getActivityData());
        }
    }

    @Override
    public ActivityData getObject() {
        return activity.getActivityData();
    }

}
