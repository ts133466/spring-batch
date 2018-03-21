package com.ifast.batch.config;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource(value= {"classpath:persistence.hibernate.properties"})
@Profile("local")
@EnableTransactionManagement
public class LocalHibernateConfig {
	
	@Inject
	protected Environment env;

	@Value("#{'com.fmy.entity'.split(',')}")
	protected List<String> packageList;

	@Bean
	public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
		PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
		propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
		return propertySourcesPlaceholderConfigurer;
	}

	@Bean
	public LocalSessionFactoryBean sessionFactory() {
		final LocalSessionFactoryBean ret = new LocalSessionFactoryBean();
		ret.setHibernateProperties(hiberateProperties());
		ret.setPackagesToScan(packageList.toArray(new String[0]));
		ret.setDataSource(dataSource());
		return ret;
	}

	@Bean
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(env.getProperty("com.ifast.local.dataSource.url.driverclassname"));
		dataSource.setUrl(env.getProperty("com.ifast.local.dataSource.url"));
		dataSource.setUsername(env.getProperty("com.ifast.local.dataSource.username"));
		dataSource.setPassword(env.getProperty("com.ifast.local.dataSource.pwd"));
		return dataSource;
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
	public HibernateTransactionManager transactionManager(final LocalSessionFactoryBean localSessionFactoryBean) {
		final HibernateTransactionManager ret = new HibernateTransactionManager();
		ret.setSessionFactory(localSessionFactoryBean.getObject());
		return ret;
	}

	protected Properties hiberateProperties() {
		Properties properties = new Properties();
		properties.setProperty("hibernate.c3p0.acquire_increment", env.getProperty("com.ifast.persistence.hibernate.connection.hibernate.cp30.acquire_increment"));
		properties.setProperty("hibernate.c3p0.idle_test_period", env.getProperty("com.ifast.persistence.hibernate.connection.hibernate.cp30.idle_test_period"));
		properties.setProperty("hibernate.c3p0.max_size", env.getProperty("com.ifast.persistence.hibernate.connection.hibernate.cp30.max_size"));
		properties.setProperty("hibernate.c3p0.max_statements", env.getProperty("com.ifast.persistence.hibernate.connection.hibernate.cp30.max_statements"));
		properties.setProperty("hibernate.c3p0.min_size", env.getProperty("com.ifast.persistence.hibernate.connection.hibernate.cp30.min_size"));
		properties.setProperty("hibernate.c3p0.timeout", env.getProperty("com.ifast.persistence.hibernate.connection.hibernate.cp30.timeout"));
		properties.setProperty("hibernate.dialect", env.getProperty("com.ifast.persistence.hibernate.dialect"));
		properties.setProperty("hibernate.hbm2ddl.auto", env.getProperty("com.ifast.persistence.hibernate.hbm2ddl.auto"));
		properties.setProperty("hibernate.show_sql", env.getProperty("com.ifast.persistence.hibernate.show_sql"));
		properties.setProperty("hibernate.format_sql", env.getProperty("com.ifast.persistence.hibernate.format_sql"));
		properties.setProperty("hibernate.search.default.directory_provider", env.getProperty("hibernate.search.default.directory_provider"));
		System.out.println(env.getProperty("hibernate.search.index.base"));
		properties.setProperty("hibernate.search.default.indexBase", env.getProperty("hibernate.search.index.base"));
		return properties;
	}
}
