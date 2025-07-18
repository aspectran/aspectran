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
 * The Class TemplateNotFoundException.
 */
public class TemplateNotFoundException extends TemplateException {

    @Serial
    private static final long serialVersionUID = 7022297599581677022L;

    private final String templateId;

    /**
     * Instantiates a new TemplateNotFoundException.
     * @param templateId the template id
     */
    public TemplateNotFoundException(String templateId) {
        super("No template named '" + templateId + "' is defined");
        this.templateId = templateId;
    }

    /**
     * Gets the template id.
     * @return the template id
     */
    public String getTemplateId() {
        return templateId;
    }

}
