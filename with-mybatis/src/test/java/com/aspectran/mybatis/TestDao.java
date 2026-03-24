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
package com.aspectran.mybatis;

import java.util.List;

public class TestDao extends SqlMapperAccess<TestMapper> implements TestMapper {

    public TestDao(TestSqlMapperProvider sqlMapperProvider) {
        super(sqlMapperProvider);
    }

    @Override
    public Member getMember(Long id) {
        return simple().getMember(id);
    }

    @Override
    public List<Member> getMemberList() {
        return simple().getMemberList();
    }

    @Override
    public int insertMember(Member member) {
        return simple().insertMember(member);
    }

}
