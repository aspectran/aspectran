package com.aspectran.mybatis;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * <p>Created: 2026. 4. 5.</p>
 */
public interface SqlSessionProvider {

    /**
     * Returns the current SqlSession bound to this advisor/context.
     * @return the active SqlSession
     */
    SqlSession getSqlSession();

    SqlSessionAdvice getSqlSessionAdvice();

    SqlSessionFactory getSqlSessionFactory();

}
