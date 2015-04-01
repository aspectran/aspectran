package com.aspectran.support.orm.mybatis;

import java.sql.SQLException;

import com.aspectran.core.activity.Translet;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 *
 */
public class SqlSessionTransactionAdvice {
	
	private SqlMapClient sqlMapClient;
	
	public SqlSessionTransactionAdvice(SqlSessionFactoryBean sqlMapConfig) {
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
