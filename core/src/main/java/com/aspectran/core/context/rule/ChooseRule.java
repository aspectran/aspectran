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

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a conditional block that functions like a switch-case statement.
 * It contains one or more {@link ChooseWhenRule}s, where the first one that
 * tests true has its actions executed.
 *
 * <p>Created: 2019-01-06</p>
 */
public class ChooseRule {

    private List<ChooseWhenRule> chooseWhenRules;

    /**
     * Creates a new ChooseWhenRule and adds it to the list of when-clauses.
     * @return the new ChooseWhenRule
     */
    public ChooseWhenRule newChooseWhenRule() {
        if (chooseWhenRules == null) {
            chooseWhenRules = new ArrayList<>();
        }
        ChooseWhenRule chooseWhenRule = new ChooseWhenRule();
        chooseWhenRules.add(chooseWhenRule);
        return chooseWhenRule;
    }

    /**
     * Gets the list of when-clauses.
     * @return the list of ChooseWhenRule objects
     */
    public List<ChooseWhenRule> getChooseWhenRules() {
        return chooseWhenRules;
    }

    /**
     * Creates a new instance of ChooseRule.
     * @return a new ChooseRule instance
     */
    @NonNull
    public static ChooseRule newInstance() {
        return new ChooseRule();
    }

}
