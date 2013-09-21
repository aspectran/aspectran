package com.aspectran.support.orm.ibatis;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 *
 */
public class AspectSqlMapClient {
	
	private SqlMapConfig sqlMapConfig;
	
	public AspectSqlMapClient(SqlMapConfig sqlMapConfig) {
		this.sqlMapConfig = sqlMapConfig;
	}

	public SqlMapClient begin() throws SQLException {
		sqlMapConfig.getSqlMapClient().startTransaction();
		
		return sqlMapConfig.getSqlMapClient();
	}
	
	public void end() throws SQLException {
		try {
			sqlMapConfig.getSqlMapClient().commitTransaction();
		} finally {
			sqlMapConfig.getSqlMapClient().endTransaction();
		}
	}

}
