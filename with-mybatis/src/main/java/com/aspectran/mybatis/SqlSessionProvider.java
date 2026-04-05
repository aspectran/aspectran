package com.aspectran.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Interface that provides access to a context-bound MyBatis {@link SqlSession}
 * and its associated {@link SqlSessionAdvice}.
 *
 * <p>Created: 2026. 4. 5.</p>
 */
public interface SqlSessionProvider {

    /**
     * Returns the current SqlSession bound to the current activity context.
     * @return the active SqlSession
     */
    SqlSession getSqlSession();

    /**
     * Returns the {@link SqlSessionAdvice} that manages the lifecycle of the
     * current SqlSession.
     * @return the SqlSessionAdvice
     */
    SqlSessionAdvice getSqlSessionAdvice();

    /**
     * Returns the {@link SqlSessionFactory} used by this provider.
     * @return the SqlSessionFactory
     */
    SqlSessionFactory getSqlSessionFactory();

}
