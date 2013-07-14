package com.aspectran.test.plugins;

import java.sql.SQLException;
import java.util.Map;

import com.aspectran.base.variable.AttributeMap;
import com.aspectran.core.translet.SuperTranslet;
import com.aspectran.test.SqlMapConfig;
import com.ibatis.sqlmap.client.SqlMapClient;

/**
 *
 * <p>Created: 2008. 06. 11 오후 3:44:05</p>
 *
 */
public class SqlMapExecutor {
	
	private static final String METHOD_PARAM = "method";

	private static final String STATEMENT_ID_PARAM = "stmtId";

	private String method;

	private String statementId;
	
	public Object execute(SuperTranslet translet, Map<String, Object> arguments) throws Exception {
		method = (String)arguments.get(METHOD_PARAM);
		statementId = (String)arguments.get(STATEMENT_ID_PARAM);

		return sqlmapExecute(translet);
	}
	
	private Object sqlmapExecute(SuperTranslet translet) throws SQLException {
		SqlMapClient sqlMap = null;
		
		try {
			sqlMap = SqlMapConfig.getInstance();
			sqlMap.startTransaction();
			
			Object result = null;

			AttributeMap params = translet.getAttributeMap();

			if("queryForObject".equals(method))
				result = sqlMap.queryForObject(statementId, params);
			else if("queryForList".equals(method))
				result = sqlMap.queryForList(statementId, params);
			else if("insert".equals(method))
				result = sqlMap.insert(statementId, params);
			else if("update".equals(method))
				result = sqlMap.update(statementId, params);
			else if("delete".equals(method))
				result = sqlMap.delete(statementId, params);
			else
				throw new SQLException("Unkown method.");
			
			sqlMap.commitTransaction();
			
			return result;
		} finally {
			try {
				if(sqlMap != null)
					sqlMap.endTransaction();
			} catch(SQLException e) {}
		}
	}
}
