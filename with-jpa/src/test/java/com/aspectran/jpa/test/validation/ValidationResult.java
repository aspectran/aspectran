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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Created: 2025-05-12</p>
 */
public class ValidationResult {

    private Map<String, String> errors;

    public boolean hasErrors() {
        return (errors != null);
    }

    public boolean hasError(String key) {
        return (errors != null && errors.containsKey(key));
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void putError(String key, String message) {
        touchErrors().put(key, message);
    }

    public void clearErrors() {
        errors.clear();
        errors = null;
    }

    private Map<String, String> touchErrors() {
        if (errors == null) {
            errors = new LinkedHashMap<>();
        }
        return errors;
    }

}
