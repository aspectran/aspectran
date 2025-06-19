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
package com.aspectran.jpa.test.vet;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.core.component.bean.annotation.Request;
import com.aspectran.core.component.bean.annotation.Transform;
import com.aspectran.core.context.rule.type.FormatType;
import com.aspectran.jpa.test.pagination.PageInfo;

import java.util.List;

/**
 * <p>Created: 2025-06-12</p>
 */
@Component
public class VetAction {

    private final VetDao vetDao;

    @Autowired
    public VetAction(VetDao vetDao) {
        this.vetDao = vetDao;
    }

    @Request("/vetList")
    @Transform(FormatType.JSON)
    public List<Vet> vetList(Translet translet) {
        PageInfo pageInfo = PageInfo.of(translet);
        return vetDao.findAll(pageInfo);
    }

}
