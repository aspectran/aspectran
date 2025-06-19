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
package com.aspectran.core.component.bean.aware;

import com.aspectran.core.adapter.ApplicationAdapter;

/**
 * Interface to be implemented by any object that wishes to be notified of the
 * {@link ApplicationAdapter}.
 *
 * <p>Created: 2016. 1. 25.</p>
 *
 * @since 2.0.0
 */
public interface ApplicationAdapterAware extends Aware {

    void setApplicationAdapter(ApplicationAdapter applicationAdapter);

}
