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
package com.aspectran.jpa.test;

import com.aspectran.jpa.common.model.Vet;
import com.aspectran.jpa.querydsl.EntityQuery;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class JpaTestDao {

    private final EntityQuery entityQuery;

    public JpaTestDao(EntityQuery entityQuery) {
        this.entityQuery = entityQuery;
    }

    public void insertVet(Vet vet) {
        entityQuery.persist(vet);
    }

    public Vet getVet(Integer id) {
        return entityQuery.find(Vet.class, id);
    }

    public List<Vet> getVetList() {
        return entityQuery.createQuery("from Vet", Vet.class).getResultList();
    }

    public void insertVets(Vet @NotNull ... vets) {
        for (Vet vet : vets) {
            insertVet(vet);
        }
    }

}
