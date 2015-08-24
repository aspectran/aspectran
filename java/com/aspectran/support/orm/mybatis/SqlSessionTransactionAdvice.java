package com.aspectran.support.orm.mybatis;

import java.sql.SQLException;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
*
* @author Gulendol
*
* <p>Created: 2015. 04. 03</p>
*
*/
public class SqlSessionTransactionAdvice {
	
	private final SqlSessionFactory sqlSessionFactory;
	
	private SqlSession sqlSession;
	
	private boolean autoCommit;
	
	public SqlSessionTransactionAdvice(SqlSessionFactoryBean factoryBean) {
		this.sqlSessionFactory = factoryBean.getObject();
	}
	
	public SqlSession getSqlSession() {
		return sqlSession;
	}

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public SqlSession open() throws SQLException {
		return sqlSessionFactory.openSession();
	}
	
	public SqlSession open(boolean autoCommit) throws SQLException {
		this.autoCommit = autoCommit;
		return sqlSessionFactory.openSession(autoCommit);
	}
	
	public void commit() throws SQLException {
		if(!autoCommit)
			sqlSession.commit();
	}
	
	public void commit(boolean force) throws SQLException {
		if(!autoCommit)
			sqlSession.commit(force);
	}
	
	public void close() throws SQLException {
		sqlSession.close();
	}
	
}
