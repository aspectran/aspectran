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
package com.aspectran.jpa.test.validation;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import java.util.Set;

@Component
@Bean
public class DefaultValidator {

    private final Validator validator;

    @Autowired
    public DefaultValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> ValidationResult validate(T model) {
        return validate(model, Default.class);
    }

    public <T> ValidationResult validate(T model, Class<?> group) {
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<T>> violations = validator.validate(model, group);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<T> violation : violations) {
                result.putError(violation.getPropertyPath().toString(), violation.getMessage());
            }
        }
        return result;
    }

}
