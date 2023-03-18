/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
package com.aspectran.embed.service.action;

import com.aspectran.core.activity.Translet;

public class TestActivity {

    public int addUp(int arg1, int arg2, int arg3, int arg4) {
        return arg1 + arg2 + arg3 + arg4;
    }

    public int getArg4() {
        return 4;
    }

    public String case1(Translet translet) {
        return translet.getParameter("mode");
    }

    public String case2(Translet translet) {
        return translet.getParameter("mode");
    }

    public String case3(Translet translet) {
        return translet.getParameter("mode");
    }

    public String case4(Translet translet) {
        return translet.getParameter("mode");
    }

    public void thrown1() {
        throw new NullPointerException("-- FOR TEST --");
    }

    public void thrown2() {
        throw new IllegalArgumentException("-- FOR TEST --");
    }

    public void thrown3() {
        throw new UnsupportedOperationException("-- FOR TEST --");
    }

}
