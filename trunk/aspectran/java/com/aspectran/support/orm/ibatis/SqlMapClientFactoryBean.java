package com.aspectran.support.orm.ibatis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.util.Assert;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;

/**
*
* @author Gulendol
*
* <p>Created: 2015. 04. 03</p>
*
*/
public class SqlMapClientFactoryBean implements InitializableTransletBean, FactoryBean<SqlMapClient> {
	
	private String configLocation;

	private Properties sqlMapClientProperties;

	private SqlMapClient sqlMapClient;

	public SqlMapClientFactoryBean() {
	}

	/**
	 * Set the location of the iBATIS SqlMapClient config file.
	 * A typical value is "/WEB-INF/sql-map-config.xml".
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set optional properties to be passed into the SqlMapClientBuilder, as
	 * alternative to a <code>&lt;properties&gt;</code> tag in the sql-map-config.xml
	 * file. Will be used to resolve placeholders in the config file.
	 * @see #setConfigLocation
	 * @see com.ibatis.sqlmap.client.SqlMapClientBuilder#buildSqlMapClient(java.io.Reader, java.util.Properties)
	 */
	public void setSqlMapClientProperties(Properties sqlMapClientProperties) {
		this.sqlMapClientProperties = sqlMapClientProperties;
	}

	public void initialize(Translet translet) throws Exception {
		Assert.notNull(configLocation, "Property 'configLocation' is required");

		InputStream is = getInputStream(configLocation, translet);

		buildSqlMapClient(is);
	}
	
	public void buildSqlMapClient(InputStream is) throws Exception {
		if(sqlMapClientProperties != null)
			sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is), sqlMapClientProperties);
		else
			sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is));
	}

	public SqlMapClient getObject() {
		return this.sqlMapClient;
	}
	
	private static InputStream getInputStream(String filePath, Translet translet) throws FileNotFoundException {
		File file = translet.getApplicationAdapter().toRealPathAsFile(filePath);
		return new FileInputStream(file);
	}
	
}