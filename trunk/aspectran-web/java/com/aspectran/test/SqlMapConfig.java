package com.aspectran.test;

import java.io.InputStream;

import com.aspectran.base.util.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 *
 * @author Gulendol
 *
 * <p>Created: 2008. 05. 14 오후 7:52:29</p>
 *
 */
public class SqlMapConfig {
	
	private static final SqlMapClient sqlMap;

	static {
		try{
			final String resource = "sqlmap/sql-map-config.xml";
			InputStream is = Resources.getResourceAsStream(resource);
			sqlMap = SqlMapClientBuilder.buildSqlMapClient(is);
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error initializing SqlMapConfig class", e);
		}
	}

	/**
	 * SqlMapClient Instance를 반환한다.
	 * @return SqlMapClient
	 */
	public static SqlMapClient getInstance(){
		return sqlMap;
	}
}