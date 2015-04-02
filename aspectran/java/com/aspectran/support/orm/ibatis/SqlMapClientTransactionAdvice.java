package com.aspectran.support.orm.ibatis;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 *
 */
public class SqlMapClientTransactionAdvice {
	
	private SqlMapClient sqlMapClient;
	
	public SqlMapClientTransactionAdvice(SqlMapClientFactoryBean factoryBean) {
		this.sqlMapClient = factoryBean.getObject();
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}

	public SqlMapClient begin() throws SQLException {
		sqlMapClient.startTransaction();
		
		return sqlMapClient;
	}
	
	public void after() throws SQLException {
		sqlMapClient.commitTransaction();
	}
	
	public void end() throws SQLException {
		sqlMapClient.endTransaction();
	}
	
}
