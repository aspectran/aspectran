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
package com.aspectran.core.context.rule.params;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

import java.util.List;

/**
 * Represents the parameters for an item holder, which can contain multiple item parameters.
 */
public class ItemHolderParameters extends AbstractParameters {

    public static final ParameterKey profile;
    public static final ParameterKey item;

    private static final ParameterKey[] parameterKeys;

    static {
        profile = new ParameterKey("profile", ValueType.STRING);
        item = new ParameterKey("item", ItemParameters.class, true, true);

        parameterKeys = new ParameterKey[] {
                profile,
                item
        };
    }

    public ItemHolderParameters() {
        super(parameterKeys);
    }

    public ItemHolderParameters(String apon) throws AponParseException {
        this();
        readFrom(apon);
    }

    public String getProfile() {
        return getString(profile);
    }

    public ItemHolderParameters setProfile(String profile) {
        putValue(ItemHolderParameters.profile, profile);
        return this;
    }

    public List<ItemParameters> getItemParametersList() {
        if (isAssigned(item)) {
            return getParametersList(item);
        } else {
            return null;
        }
    }

    public ItemHolderParameters addItemParameters(ItemParameters itemParameters) {
        putValue(item, itemParameters);
        return this;
    }

    public ItemParameters newItemParameters() {
        return newParameters(item);
    }

}
