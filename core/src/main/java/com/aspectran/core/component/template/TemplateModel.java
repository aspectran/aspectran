/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.core.component.template;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityData;

import java.util.Locale;

/**
 * The Class TemplateModel.
 */
public class TemplateModel extends ActivityData {

    /** @serial */
    private static final long serialVersionUID = -1414688689441309354L;

    public TemplateModel(Activity activity) {
        super(activity);
    }

    public Locale getLocale() {
        if (getActivity().getRequestAdapter() != null) {
            return getActivity().getRequestAdapter().getLocale();
        }
        return null;
    }

}
