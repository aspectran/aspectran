package com.aspectran.jpa;

import com.aspectran.core.component.bean.annotation.Advisable;
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

    @Override
    public <T> JPAQuery<T> selectDistinct(Expression<T> expr) {
        return select(expr).distinct();
    }

    @Override
    public JPAQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return select(exprs).distinct();
    }

    @Override
    public JPAQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    @Override
    public JPAQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    @Override
    public <T> JPAQuery<T> selectFrom(EntityPath<T> from) {
        return select(from).from(from);
    }

    @Override
    public JPAQuery<?> from(EntityPath<?> from) {
        return query().from(from);
    }

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
