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

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableFactoryBean;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.support.i18n.message.MessageSource;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;

@Component
@Bean
public class ValidatorFactoryBean implements ActivityContextAware, InitializableFactoryBean<Validator>, DisposableBean {

    private final MessageSource messageSource;

    private ActivityContext context;

    private ValidatorFactory validatorFactory;

    private Validator validator;

    @Autowired(required = false)
    public ValidatorFactoryBean(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        if (validator == null) {
            if (validatorFactory == null) {
                validatorFactory = Validation.buildDefaultValidatorFactory();
            }
            if (messageSource != null) {
                ValidatorContext validatorContext = validatorFactory.usingContext();
                MessageInterpolator targetInterpolator = validatorFactory.getMessageInterpolator();

                LocaleContextMessageInterpolator messageInterpolator = new LocaleContextMessageInterpolator(targetInterpolator);
                messageInterpolator.setActivityContext(context);

                validatorContext.messageInterpolator(messageInterpolator);
            }
            validator = validatorFactory.getValidator();
        }
    }

    @Override
    public Validator getObject() {
        return validator;
    }


    @Override
    public void destroy() throws Exception {
        if (validatorFactory != null) {
            validatorFactory.close();
            validatorFactory = null;
            validator = null;
        }
    }

}
