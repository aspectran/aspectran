package com.aspectran.support.orm.ibatis;

import java.util.Map;

import com.aspectran.web.activity.WebTranslet;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 * <p>Created: 2008. 06. 11 오후 3:44:05</p>
 *
 */
public class SqlMapClientTransaction {
	
	private static final String METHOD_PARAM = "method";

	private static final String STATEMENT_ID_PARAM = "stmtId";

	private SqlMapClient sqlMapClient;
	
	public void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClient = sqlMapClient;
	}
	
	public void begin() {
		
	}
	
	public void end() {
		
	}
	
	public Object select(WebTranslet translet, Map<String, Object> arguments) throws Exception {
		String sqlmap = (String)arguments.get("sqlmap");
		Map<String, Object> param = (Map<String, Object>)arguments.get("param");
		
		translet.getBeforeAdviceResult(sqlmap);
		
		if(param == null) {
			
		}
		
		return sqlMapClient.queryForObject("");
	}

}
