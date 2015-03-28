package com.aspectran.support.orm.ibatis;

import java.sql.SQLException;

import com.aspectran.core.activity.Translet;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 *
 */
public class SqlMapClientTransactionAdvice {
	
	private SqlMapClient sqlMapClient;
	
	public SqlMapClientTransactionAdvice(SqlMapClientProvider sqlMapConfig) {
		this.sqlMapClient = sqlMapConfig.getSqlMapClient();
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
	
	public void end(Translet translet) throws SQLException {
		if(!translet.isExceptionRaised())
			sqlMapClient.commitTransaction();
		
		sqlMapClient.endTransaction();
	}
	
}
