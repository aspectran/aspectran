package com.aspectran.support.orm.ibatis;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 *
 */
public class SqlMapClientTransactionAdvice {
	
	private SqlMapClient sqlMapClient;
	
	public SqlMapClientTransactionAdvice(SqlMapConfig sqlMapConfig) {
		this.sqlMapClient = sqlMapConfig.getSqlMapClient();
	}
	
	public SqlMapClient getSqlMapClient() {
		return sqlMapClient;
	}

	public SqlMapClient startTransaction() throws SQLException {
		sqlMapClient.startTransaction();
		
		return sqlMapClient;
	}
	
	public void commitTransaction() throws SQLException {
		sqlMapClient.commitTransaction();
	}
	
	public void endTransaction() throws SQLException {
		sqlMapClient.endTransaction();
	}

}
