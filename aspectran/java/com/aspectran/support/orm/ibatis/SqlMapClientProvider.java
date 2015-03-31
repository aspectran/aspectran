package com.aspectran.support.orm.ibatis;

import java.io.InputStream;

import com.aspectran.core.util.ResourceUtils;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 *
 * @author Gulendol
 *
 * <p>Created: 2008. 05. 14 오후 7:52:29</p>
 *
 */
public class SqlMapClientProvider {
	
	private String configLocation;
	
	private String[] mapperLocations;
	
	private SqlMapClient sqlMapClient;

	public SqlMapClientProvider(String resource) {
		buildSqlMapClient(resource);
	}
	
	
	
	protected void buildSqlMapClient(String resource) {
		try {
			ClassLoader classLoader = ResourceUtils.getClassLoader(this.getClass());
			InputStream is = classLoader.getResourceAsStream(resource);
			sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(is);
		} catch(Exception e) {
			throw new RuntimeException("Error initializing SqlMapClient instance", e);
		}
	}
	
	public SqlMapClient getSqlMapClient(){
		return sqlMapClient;
	}
}