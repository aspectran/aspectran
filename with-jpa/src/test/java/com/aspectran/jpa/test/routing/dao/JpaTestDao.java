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
package com.aspectran.jpa.test.routing.dao;

import com.aspectran.jpa.test.common.model.Vet;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class JpaTestDao {

    private final EntityManager entityManager;

    public JpaTestDao(EntityManager entityQuery) {
        this.entityManager = entityQuery;
    }

    public void insertVet(Vet vet) {
        entityManager.persist(vet);
    }

    public Vet getVet(Integer id) {
        return entityManager.find(Vet.class, id);
    }

    public List<Vet> getVetList() {
        return getVetList(null);
    }

    public List<Vet> getVetList(java.util.Map<String, Object> hints) {
        jakarta.persistence.TypedQuery<Vet> query = entityManager.createQuery("from Vet", Vet.class);
        if (hints != null) {
            hints.forEach(query::setHint);
        }
        return query.getResultList();
    }

    public void insertVets(Vet @NonNull ... vets) {
        for (Vet vet : vets) {
            insertVet(vet);
        }
    }

}
