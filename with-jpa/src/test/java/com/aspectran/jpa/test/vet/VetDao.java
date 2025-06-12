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
package com.aspectran.jpa.test.vet;

import com.aspectran.core.component.bean.annotation.Autowired;
import com.aspectran.core.component.bean.annotation.Component;
import com.aspectran.jpa.test.hibernate.DefaultEntityQuery;
import com.aspectran.jpa.test.pagination.PageInfo;
import com.aspectran.jpa.querydsl.EntityQuery;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.querydsl.core.Fetchable;

import java.util.List;

@Component
public class VetDao {

    private final EntityQuery entityQuery;

    @Autowired
    public VetDao(DefaultEntityQuery entityQuery) {
        this.entityQuery = entityQuery;
    }

    public Vet findById(int id) {
        return entityQuery.find(Vet.class, id);
    }

    public List<Vet> findAll(@NonNull PageInfo pageInfo) {
        QVet vet = QVet.vet;
        List<Vet> listVets = entityQuery
                .selectFrom(vet)
                .offset(pageInfo.getOffset())
                .limit(pageInfo.getSize())
                .fetch();

        Fetchable<Long> countQuery = entityQuery
                .select(vet.count())
                .from(vet);

        pageInfo.setTotalElements(listVets.size(), countQuery::fetchOne);
        return listVets;
    }

}
