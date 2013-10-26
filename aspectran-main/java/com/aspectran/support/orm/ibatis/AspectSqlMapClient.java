package com.aspectran.support.orm.ibatis;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 *
 */
public class AspectSqlMapClient {
	
	private SqlMapClient sqlMapClient;
	
	public AspectSqlMapClient(SqlMapConfig sqlMapConfig) {
		this.sqlMapClient = sqlMapConfig.getSqlMapClient();
	}

	public SqlMapClient begin() throws SQLException {
		sqlMapClient.startTransaction();
		
		return sqlMapClient;
	}
	
	public void end() throws SQLException {
		try {
			sqlMapClient.commitTransaction();
		} finally {
			sqlMapClient.endTransaction();
		}
	}

}
