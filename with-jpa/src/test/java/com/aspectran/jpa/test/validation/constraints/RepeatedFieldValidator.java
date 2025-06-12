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
package com.aspectran.jpa.test.validation.constraints;

import com.aspectran.utils.BeanUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class RepeatedFieldValidator implements ConstraintValidator<RepeatedField, Object> {

	private String field;

	private String dependField;

	private String message;

	@Override
	public void initialize(@NonNull RepeatedField constraintAnnotation) {
		this.field = constraintAnnotation.field();
		this.dependField = constraintAnnotation.dependField();
		this.message = constraintAnnotation.message();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		Object fieldValue;
		try {
			fieldValue = BeanUtils.getProperty(value, field);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		Object dependFieldValue;
		try {
			dependFieldValue = BeanUtils.getProperty(value, dependField);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		boolean matched = Objects.equals(fieldValue, dependFieldValue);
		if (matched) {
			return true;
		} else {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(message)
					.addPropertyNode(field)
					.addConstraintViolation();
			return false;
		}
	}

}
