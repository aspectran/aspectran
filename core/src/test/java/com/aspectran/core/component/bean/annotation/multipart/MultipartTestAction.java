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
package com.aspectran.core.component.bean.annotation.multipart;

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.core.activity.request.FileParameterMap;
import com.aspectran.core.component.bean.annotation.Action;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Multipart;
import com.aspectran.core.component.bean.annotation.RequestToPost;

@Component
public class MultipartTestAction {

    @RequestToPost("/test/upload1")
    @Multipart
    @Action
    public void uploadWithDefaultParser() {
    }

    @RequestToPost("/test/upload2")
    @Multipart("customParser")
    @Action
    public void uploadWithCustomParser() {
    }

    @RequestToPost("/test/upload3")
    @Action
    public void uploadWithFileParameter(FileParameter file) {
    }

    @RequestToPost("/test/upload4")
    @Action
    public void uploadWithFileParameterArray(FileParameter[] files) {
    }

    @RequestToPost("/test/upload5")
    @Action
    public void uploadWithFileParameterMap(FileParameterMap fileMap) {
    }

    @RequestToPost("/test/normal")
    @Action
    public void normalAction(String name) {
    }

}
