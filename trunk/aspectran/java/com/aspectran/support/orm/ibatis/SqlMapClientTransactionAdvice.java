package com.aspectran.support.orm.ibatis;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
*
* @author Gulendol
*
* <p>Created: 2015. 04. 03</p>
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

	public SqlMapClient start() throws SQLException {
		sqlMapClient.startTransaction();
		
		return sqlMapClient;
	}
	
	public void commit() throws SQLException {
		sqlMapClient.commitTransaction();
	}
	
	public void end() throws SQLException {
		sqlMapClient.endTransaction();
	}
	
}
