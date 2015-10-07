/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.support.orm.mybatis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.type.TypeHandler;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.bean.ablility.FactoryBean;
import com.aspectran.core.context.bean.ablility.InitializableTransletBean;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * {@code FactoryBean} that creates an MyBatis {@code SqlSessionFactory}.
 * This is the usual way to set up a shared MyBatis {@code SqlSessionFactory} in a Spring application context;
 * the SqlSessionFactory can then be passed to MyBatis-based DAOs via dependency injection.
 *
 * Either {@code DataSourceTransactionManager} or {@code JtaTransactionManager} can be used for transaction
 * demarcation in combination with a {@code SqlSessionFactory}. JTA should be used for transactions
 * which span multiple databases or when container managed transactions (CMT) are being used.
 *
 * @author Putthibong Boonbong
 * @author Hunter Presnall
 * @author Eduardo Macarron
 * 
 * @see #setConfigLocation
 * @see #setDataSource
 * @version $Id$
 */
public class SqlSessionFactoryBean implements InitializableTransletBean, FactoryBean<SqlSessionFactory> {
	
	private static final String CONFIG_LOCATION_DELIMITERS = ",;";
	
	private static final Log log = LogFactory.getLog(SqlSessionFactoryBean.class);

	private String configLocation;

	private String[] mapperLocations;

	private DataSource dataSource;

	private TransactionFactory transactionFactory;

	private Properties configurationProperties;

	private SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();

	private SqlSessionFactory sqlSessionFactory;

	// EnvironmentAware requires spring 3.1
	private String environment = SqlSessionFactoryBean.class.getSimpleName();

	private Interceptor[] plugins;

	private TypeHandler<?>[] typeHandlers;

	private String typeHandlersPackage;

	private Class<?>[] typeAliases;

	private String typeAliasesPackage;

	private Class<?> typeAliasesSuperType;

	// issue #19. No default provider.
	private DatabaseIdProvider databaseIdProvider;

	private ObjectFactory objectFactory;

	private ObjectWrapperFactory objectWrapperFactory;

	/**
	 * Sets the ObjectFactory.
	 * 
	 * @since 1.1.2
	 * @param objectFactory
	 */
	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	/**
	 * Sets the ObjectWrapperFactory.
	 * 
	 * @since 1.1.2
	 * @param objectWrapperFactory
	 */
	public void setObjectWrapperFactory(
			ObjectWrapperFactory objectWrapperFactory) {
		this.objectWrapperFactory = objectWrapperFactory;
	}

	/**
	 * Gets the DatabaseIdProvider
	 *
	 * @since 1.1.0
	 * @return
	 */
	public DatabaseIdProvider getDatabaseIdProvider() {
		return databaseIdProvider;
	}

	/**
	 * Sets the DatabaseIdProvider. As of version 1.2.2 this variable is not
	 * initialized by default.
	 *
	 * @since 1.1.0
	 * @param databaseIdProvider
	 */
	public void setDatabaseIdProvider(DatabaseIdProvider databaseIdProvider) {
		this.databaseIdProvider = databaseIdProvider;
	}

	/**
	 * Mybatis plugin list.
	 *
	 * @since 1.0.1
	 *
	 * @param plugins
	 *            list of plugins
	 *
	 */
	public void setPlugins(Interceptor[] plugins) {
		this.plugins = plugins;
	}

	/**
	 * Packages to search for type aliases.
	 *
	 * @since 1.0.1
	 *
	 * @param typeAliasesPackage
	 *            package to scan for domain objects
	 *
	 */
	public void setTypeAliasesPackage(String typeAliasesPackage) {
		this.typeAliasesPackage = typeAliasesPackage;
	}

	/**
	 * Super class which domain objects have to extend to have a type alias
	 * created. No effect if there is no package to scan configured.
	 *
	 * @since 1.1.2
	 *
	 * @param typeAliasesSuperType
	 *            super class for domain objects
	 *
	 */
	public void setTypeAliasesSuperType(Class<?> typeAliasesSuperType) {
		this.typeAliasesSuperType = typeAliasesSuperType;
	}

	/**
	 * Packages to search for type handlers.
	 *
	 * @since 1.0.1
	 *
	 * @param typeHandlersPackage
	 *            package to scan for type handlers
	 *
	 */
	public void setTypeHandlersPackage(String typeHandlersPackage) {
		this.typeHandlersPackage = typeHandlersPackage;
	}

	/**
	 * Set type handlers. They must be annotated with {@code MappedTypes} and
	 * optionally with {@code MappedJdbcTypes}
	 *
	 * @since 1.0.1
	 *
	 * @param typeHandlers
	 *            Type handler list
	 */
	public void setTypeHandlers(TypeHandler<?>[] typeHandlers) {
		this.typeHandlers = typeHandlers;
	}

	/**
	 * List of type aliases to register. They can be annotated with
	 * {@code Alias}
	 *
	 * @since 1.0.1
	 *
	 * @param typeAliases
	 *            Type aliases list
	 */
	public void setTypeAliases(Class<?>[] typeAliases) {
		this.typeAliases = typeAliases;
	}

	/**
	 * Set the location of the MyBatis {@code SqlSessionFactory} config file. A
	 * typical value is "WEB-INF/mybatis-configuration.xml".
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set locations of MyBatis mapper files that are going to be merged into
	 * the {@code SqlSessionFactory} configuration at runtime.
	 *
	 * This is an alternative to specifying "&lt;sqlmapper&gt;" entries in an
	 * MyBatis config file. This property being based on Spring's resource
	 * abstraction also allows for specifying resource patterns here: e.g.
	 * "classpath*:sqlmap/*-mapper.xml".
	 */
	public void setMapperLocations(String[] mapperLocations) {
		this.mapperLocations = mapperLocations;
	}

	/**
	 * Set optional properties to be passed into the SqlSession configuration,
	 * as alternative to a {@code &lt;properties&gt;} tag in the configuration
	 * xml file. This will be used to resolve placeholders in the config file.
	 */
	public void setConfigurationProperties(
			Properties sqlSessionFactoryProperties) {
		this.configurationProperties = sqlSessionFactoryProperties;
	}

	/**
	 * Set the JDBC {@code DataSource} that this instance should manage
	 * transactions for. The {@code DataSource} should match the one used by the
	 * {@code SqlSessionFactory}: for example, you could specify the same JNDI
	 * DataSource for both.
	 *
	 * A transactional JDBC {@code Connection} for this {@code DataSource} will
	 * be provided to application code accessing this {@code DataSource}
	 * directly via {@code DataSourceUtils} or
	 * {@code DataSourceTransactionManager}.
	 *
	 * The {@code DataSource} specified here should be the target
	 * {@code DataSource} to manage transactions for, not a
	 * {@code TransactionAwareDataSourceProxy}. Only data access code may work
	 * with {@code TransactionAwareDataSourceProxy}, while the transaction
	 * manager needs to work on the underlying target {@code DataSource}. If
	 * there's nevertheless a {@code TransactionAwareDataSourceProxy} passed in,
	 * it will be unwrapped to extract its target {@code DataSource}.
	 *
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Sets the {@code SqlSessionFactoryBuilder} to use when creating the
	 * {@code SqlSessionFactory}.
	 *
	 * This is mainly meant for testing so that mock SqlSessionFactory classes
	 * can be injected. By default, {@code SqlSessionFactoryBuilder} creates
	 * {@code DefaultSqlSessionFactory} instances.
	 *
	 */
	public void setSqlSessionFactoryBuilder(SqlSessionFactoryBuilder sqlSessionFactoryBuilder) {
		this.sqlSessionFactoryBuilder = sqlSessionFactoryBuilder;
	}

	/**
	 * Set the MyBatis TransactionFactory to use. Default is
	 * {@code SpringManagedTransactionFactory}
	 *
	 * The default {@code SpringManagedTransactionFactory} should be appropriate
	 * for all cases: be it Spring transaction management, EJB CMT or plain JTA.
	 * If there is no active transaction, SqlSession operations will execute SQL
	 * statements non-transactionally.
	 *
	 * <b>It is strongly recommended to use the default
	 * {@code TransactionFactory}.</b> If not used, any attempt at getting an
	 * SqlSession through Spring's MyBatis framework will throw an exception if
	 * a transaction is active.
	 *
	 * @see SpringManagedTransactionFactory
	 * @param transactionFactory
	 *            the MyBatis TransactionFactory
	 */
	public void setTransactionFactory(TransactionFactory transactionFactory) {
		this.transactionFactory = transactionFactory;
	}

	/**
	 * <b>NOTE:</b> This class <em>overrides</em> any {@code Environment} you
	 * have set in the MyBatis config file. This is used only as a placeholder
	 * name. The default value is
	 * {@code SqlSessionFactoryBean.class.getSimpleName()}.
	 *
	 * @param environment
	 *            the environment name
	 */
	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(Translet translet) throws Exception {
		Assert.notNull(dataSource, "Property 'dataSource' is required");
		Assert.notNull(sqlSessionFactoryBuilder, "Property 'sqlSessionFactoryBuilder' is required");

		InputStream configLocationStream = null;
		InputStream[] mapperLocationStreams = null;
		
		if(configLocation != null) {
			File file = translet.getApplicationAdapter().toRealPathAsFile(configLocation);
			configLocationStream = new FileInputStream(file);
		}
		
		if(mapperLocations != null && mapperLocations.length > 0) {
			mapperLocationStreams = new InputStream[mapperLocations.length];
			
			for(int i = 0; i < mapperLocations.length; i++) {
				if(mapperLocations[i] != null) {
					File file = translet.getApplicationAdapter().toRealPathAsFile(mapperLocations[i]);
					mapperLocationStreams[i] = new FileInputStream(file);
				}
			}
		}
		
		this.sqlSessionFactory = buildSqlSessionFactory(configLocationStream, mapperLocationStreams);
	}

	/**
	 * Build a {@code SqlSessionFactory} instance.
	 *
	 * The default implementation uses the standard MyBatis
	 * {@code XMLConfigBuilder} API to build a {@code SqlSessionFactory}
	 * instance based on an Reader.
	 *
	 * @return SqlSessionFactory
	 * @throws IOException if loading the config file failed
	 */
	protected SqlSessionFactory buildSqlSessionFactory(InputStream configLocationStream, InputStream[] mapperLocationStreams) throws IOException {
		Configuration configuration;

		XMLConfigBuilder xmlConfigBuilder = null;
		if(configLocationStream != null) {
			xmlConfigBuilder = new XMLConfigBuilder(configLocationStream, null, this.configurationProperties);
			configuration = xmlConfigBuilder.getConfiguration();
		} else {
			if(log.isDebugEnabled()) {
				log.debug("Property 'configLocation' not specified, using default MyBatis Configuration");
			}
			configuration = new Configuration();
			configuration.setVariables(this.configurationProperties);
		}

		if(this.objectFactory != null) {
			configuration.setObjectFactory(this.objectFactory);
		}

		if(this.objectWrapperFactory != null) {
			configuration.setObjectWrapperFactory(this.objectWrapperFactory);
		}

		if(StringUtils.hasLength(this.typeAliasesPackage)) {
			String[] typeAliasPackageArray = StringUtils.tokenize(this.typeAliasesPackage, CONFIG_LOCATION_DELIMITERS);
			for(String packageToScan : typeAliasPackageArray) {
				configuration.getTypeAliasRegistry().registerAliases(
						packageToScan,
						typeAliasesSuperType == null ? Object.class : typeAliasesSuperType);
				if(log.isDebugEnabled()) {
					log.debug("Scanned package: '" + packageToScan + "' for aliases");
				}
			}
		}

		if(this.typeAliases != null && this.typeAliases.length > 0) {
			for(Class<?> typeAlias : this.typeAliases) {
				configuration.getTypeAliasRegistry().registerAlias(typeAlias);
				if(log.isDebugEnabled()) {
					log.debug("Registered type alias: '" + typeAlias + "'");
				}
			}
		}

		if(this.plugins != null && this.plugins.length > 0) {
			for(Interceptor plugin : this.plugins) {
				configuration.addInterceptor(plugin);
				if(log.isDebugEnabled()) {
					log.debug("Registered plugin: '" + plugin + "'");
				}
			}
		}

		if(StringUtils.hasLength(this.typeHandlersPackage)) {
			String[] typeHandlersPackageArray = StringUtils.tokenize(this.typeHandlersPackage, CONFIG_LOCATION_DELIMITERS);
			for(String packageToScan : typeHandlersPackageArray) {
				configuration.getTypeHandlerRegistry().register(packageToScan);
				if(log.isDebugEnabled()) {
					log.debug("Scanned package: '" + packageToScan
							+ "' for type handlers");
				}
			}
		}

		if(this.typeHandlers != null && this.typeHandlers.length > 0) {
			for(TypeHandler<?> typeHandler : this.typeHandlers) {
				configuration.getTypeHandlerRegistry().register(typeHandler);
				if(log.isDebugEnabled()) {
					log.debug("Registered type handler: '" + typeHandler
							+ "'");
				}
			}
		}

		if(xmlConfigBuilder != null) {
			try {
				xmlConfigBuilder.parse();

				if(log.isDebugEnabled()) {
					log.debug("Parsed configuration file: '"
							+ this.configLocation + "'");
				}
			} catch(Exception ex) {
				throw new IllegalArgumentException("Failed to parse config resource: " + this.configLocation, ex);
			} finally {
				ErrorContext.instance().reset();
			}
		}

		if(this.transactionFactory == null) {
			this.transactionFactory = new JdbcTransactionFactory();
		}

		configuration.setEnvironment(new Environment(this.environment, this.transactionFactory, this.dataSource));

		if(this.databaseIdProvider != null) {
			try {
				configuration.setDatabaseId(this.databaseIdProvider.getDatabaseId(this.dataSource));
			} catch(SQLException e) {
				throw new IllegalArgumentException("Failed getting a databaseId", e);
			}
		}

		if(mapperLocationStreams != null && mapperLocationStreams.length > 0) {
			for(int i = 0; i < mapperLocationStreams.length; i++) {
				if(mapperLocationStreams[i] == null)
					continue;

				try {
					XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(mapperLocationStreams[i], configuration, mapperLocations[i], configuration.getSqlFragments());
					xmlMapperBuilder.parse();
				} catch(Exception e) {
					throw new IllegalArgumentException("Failed to parse mapping resource: '" + mapperLocations[i] + "'", e);
				} finally {
					ErrorContext.instance().reset();
				}

				if(log.isDebugEnabled()) {
					log.debug("Parsed mapper file: '" + mapperLocations[i] + "'");
				}
			}
		} else {
			if(log.isDebugEnabled()) {
				log.debug("Property 'mapperLocations' was not specified or no matching resources found");
			}
		}

		return this.sqlSessionFactoryBuilder.build(configuration);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public SqlSessionFactory getObject() {
		return this.sqlSessionFactory;
	}

}