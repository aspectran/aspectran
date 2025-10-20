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
package com.aspectran.core.component.session.redis.lettuce.junit;

import com.aspectran.utils.annotation.jsr305.NonNull;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * {@link ExecutionCondition} for {@link EnabledIfDockerAvailable @EnabledIfDockerAvailable}.
 */
public class EnabledIfDockerAvailableCondition implements ExecutionCondition {

    @Override
    @NonNull
    public ConditionEvaluationResult evaluateExecutionCondition(@NonNull ExtensionContext context) {
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                return ConditionEvaluationResult.enabled("Docker is available");
            } else {
                return ConditionEvaluationResult.disabled("Docker is not available");
            }
        } catch (Throwable e) {
            return ConditionEvaluationResult.disabled("Docker is not available: " + e.getMessage());
        }
    }

}
