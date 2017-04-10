/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.core.context.parser;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.parser.apon.params.AspectranParameters;
import com.aspectran.core.context.parser.assistant.ContextParserAssistant;
import com.aspectran.core.context.env.ContextEnvironment;

/**
 * The Interface ActivityContextParser.
 * 
 * <p>Created: 2008. 06. 14 PM 8:53:29</p>
 */
public interface ActivityContextParser {

    ContextEnvironment getContextEnvironment();

    ContextParserAssistant getContextBuilderAssistant();

    void setActiveProfiles(String... activeProfiles);

    void setDefaultProfiles(String... defaultProfiles);

    void setHybridLoad(boolean hybridLoad);

    ActivityContext parse(String rootContext) throws ActivityContextParserException;

    ActivityContext parse(AspectranParameters aspectranParameters) throws ActivityContextParserException;

}
