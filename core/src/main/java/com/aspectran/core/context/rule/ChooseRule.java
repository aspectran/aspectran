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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Created: 2019-01-06</p>
 */
public class ChooseRule {

    private List<ChooseWhenRule> chooseWhenRules;

    public ChooseWhenRule newChooseWhenRule() {
        if (chooseWhenRules == null) {
            chooseWhenRules = new ArrayList<>();
        }
        ChooseWhenRule chooseWhenRule = new ChooseWhenRule();
        chooseWhenRules.add(chooseWhenRule);
        return chooseWhenRule;
    }

    public List<ChooseWhenRule> getChooseWhenRules() {
        return chooseWhenRules;
    }

    @NonNull
    public static ChooseRule newInstance() {
        return new ChooseRule();
    }

}
