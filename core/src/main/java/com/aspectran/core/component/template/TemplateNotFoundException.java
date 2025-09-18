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
package com.aspectran.core.component.template;

import java.io.Serial;

/**
 * Exception thrown when a template is not found.
 * This exception is thrown if a template with the specified ID cannot be located.
 */
public class TemplateNotFoundException extends TemplateException {

    @Serial
    private static final long serialVersionUID = 7022297599581677022L;

    private final String templateId;

    /**
     * Instantiates a new TemplateNotFoundException.
     * @param templateId the ID of the template that could not be found
     */
    public TemplateNotFoundException(String templateId) {
        super("No template named '" + templateId + "' is defined");
        this.templateId = templateId;
    }

    /**
     * Returns the ID of the template that could not be found.
     * @return the template ID
     */
    public String getTemplateId() {
        return templateId;
    }

}
