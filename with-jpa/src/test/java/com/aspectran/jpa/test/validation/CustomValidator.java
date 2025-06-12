/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.jpa.test.validation;

import com.aspectran.core.activity.Translet;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * <p>Created: 2025-05-14</p>
 */
public abstract class CustomValidator<T> {

    public abstract void validate(@NonNull Translet translet, @NonNull T model, ValidationResult result);

    public ValidationResult validate(@NonNull Translet translet, @NonNull T model) {
        ValidationResult result = new ValidationResult();
        validate(translet, model, result);
        return result;
    }

}
