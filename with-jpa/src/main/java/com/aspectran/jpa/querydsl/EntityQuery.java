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
package com.aspectran.jpa.querydsl;

import com.aspectran.core.component.bean.annotation.Advisable;
import com.aspectran.jpa.EntityManagerAgent;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQueryFactory;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAInsertClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;

/**
 * A unified implementation of the EntityManager and JPQLQueryFactory interfaces.
 *
 * <p>Created: 2025-04-24</p>
 */
public class EntityQuery extends EntityManagerAgent implements JPQLQueryFactory {

    private JPQLTemplates templates;

    public EntityQuery(String relevantAspectId) {
        super(relevantAspectId);
    }

    public void setTemplates(JPQLTemplates templates) {
        this.templates = templates;
    }

    @Advisable
    @Override
    public JPADeleteClause delete(EntityPath<?> path) {
        getEntityManagerAdvice().transactional();
        if (templates != null) {
            return new JPADeleteClause(getEntityManager(), path, templates);
        } else {
            return new JPADeleteClause(getEntityManager(), path);
        }
    }

    @Override
    public <T> JPAQuery<T> select(Expression<T> expr) {
        return query().select(expr);
    }

    @Advisable
    @Override
    public JPAQuery<Tuple> select(Expression<?>... exprs) {
        return query().select(exprs);
    }

    @Advisable
    @Override
    public <T> JPAQuery<T> selectDistinct(Expression<T> expr) {
        return select(expr).distinct();
    }

    @Advisable
    @Override
    public JPAQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return select(exprs).distinct();
    }

    @Advisable
    @Override
    public JPAQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    @Advisable
    @Override
    public JPAQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Advisable
    @Override
    public <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return select(from).from(from);
    }

    @Advisable
    @Override
    public JPAQuery<?> from(EntityPath<?> from) {
        return query().from(from);
    }

    @Advisable
    @Override
    public JPAQuery<?> from(EntityPath<?>... from) {
        return query().from(from);
    }

    @Advisable
    @Override
    public JPAUpdateClause update(EntityPath<?> path) {
        getEntityManagerAdvice().transactional();
        if (templates != null) {
            return new JPAUpdateClause(getEntityManager(), path, templates);
        } else {
            return new JPAUpdateClause(getEntityManager(), path);
        }
    }

    @Advisable
    @Override
    public JPAInsertClause insert(EntityPath<?> path) {
        getEntityManagerAdvice().transactional();
        if (templates != null) {
            return new JPAInsertClause(getEntityManager(), path, templates);
        } else {
            return new JPAInsertClause(getEntityManager(), path);
        }
    }

    @Advisable
    @Override
    public JPAQuery<?> query() {
        if (templates != null) {
            return new JPAQuery<Void>(getEntityManager(), templates);
        } else {
            return new JPAQuery<Void>(getEntityManager());
        }
    }

}
