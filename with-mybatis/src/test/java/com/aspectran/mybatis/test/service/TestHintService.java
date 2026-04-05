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
package com.aspectran.mybatis.test.service;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Bean;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Hint;
import com.aspectran.mybatis.test.dao.TestDao;
import com.aspectran.mybatis.test.model.Member;
import org.jspecify.annotations.NonNull;

import java.util.List;

@Component
@Bean
public class TestHintService {

    private final TestDao testDao;

    @Autowired
    public TestHintService(TestDao testDao) {
        this.testDao = testDao;
    }

    @Hint(type = "transactional", value = "readOnly: true")
    public List<Member> getMembers() {
        return testDao.getMemberList();
    }

    @Hint(type = "transactional", value = "readOnly: true")
    public void addMember(@NonNull Member member) {
        testDao.insertMember(member);
    }

}
