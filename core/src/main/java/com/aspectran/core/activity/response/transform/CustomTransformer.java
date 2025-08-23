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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.Activity;

/**
 * Defines the contract for custom transformation of activity results.
 *
 * <p>Implementations of this interface provide a flexible mechanism to define
 * custom logic for converting the data produced by an {@link Activity}
 * into any desired output format. This allows developers to integrate
 * proprietary or specialized transformation processes into Aspectran's
 * response generation pipeline.</p>
 *
 * <p>Created: 2019. 06. 15</p>
 */
public interface CustomTransformer {

    void transform(Activity activity) throws Exception;

}
