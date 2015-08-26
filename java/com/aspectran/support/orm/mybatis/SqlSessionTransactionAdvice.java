/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
