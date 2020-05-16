/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

import com.aspectran.core.context.rule.type.AppendableFileFormatType;

/**
 * Defines a rule to append a file with defined rules.
 * 
 * <p>Created: 2017. 05. 06.</p>
 */
public class FileAppendRule extends AppendRule {

    public FileAppendRule(String file) {
        this(file, null, null);
    }

    public FileAppendRule(String file, String profile) {
        this(file, null, profile);
    }

    public FileAppendRule(String file, AppendableFileFormatType format) {
        this(file, format, null);
    }

    public FileAppendRule(String file, AppendableFileFormatType format, String profile) {
        setFile(file);
        setFormat(format);
        setProfile(profile);
    }

}
