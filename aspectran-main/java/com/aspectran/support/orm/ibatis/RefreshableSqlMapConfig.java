package com.aspectran.support.orm.ibatis;

import java.io.InputStream;

import com.aspectran.core.util.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
 *
 * @author Gulendol
 *
 * <p>Created: 2008. 05. 14 오후 7:52:29</p>
 *
 */
public class RefreshableSqlMapConfig extends SqlMapConfig {
	
	public RefreshableSqlMapConfig(String resource) {
		super(resource);
	}
	
}