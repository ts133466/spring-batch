package com.ifast.batch.main;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.AutomaticJobRegistrar;
import org.springframework.batch.core.configuration.support.DefaultJobLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.ifast.batch.config.BaseConfiguration;
import com.ifast.batch.config.CoreConfig;
import com.ifast.batch.dao.DaoConfig;
import com.ifast.batch.job.parallel.SampleReadWriteCopyDeleteJob;
import com.ifast.batch.listener.JobLoggingApplicationListener;
import com.ifast.batch.monitoring.RunningExecutionTracker;
import com.ifast.batch.rest.JobMonitoringRestController;
import com.ifast.batch.rest.JobOperationRestController;

@SpringBootApplication
@EnableBatchProcessing(modular = true)
@Import({ DaoConfig.class, CoreConfig.class, JobLoggingApplicationListener.class, SampleReadWriteCopyDeleteJob.class })
@EnableScheduling
@ComponentScan
@EntityScan({ "com.ifast.batch.entity" })
@EnableIntegration
public class Application extends SpringBootServletInitializer {

	@Inject
	BaseConfiguration baseConfig;

	@Inject
	RunningExecutionTracker runningExecutionTracker;

	@Inject
	JobRegistry jobRegistry;

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public SessionFactory sessionFactory(HibernateEntityManagerFactory hemf) {
		return hemf.getSessionFactory();
	}

	@Bean
	public JobMonitoringRestController jobMonitoringController(){
		return new JobMonitoringRestController(baseConfig.jobOperator(), 
				baseConfig.jobExplorer(), runningExecutionTracker);
	}

	@Bean
	public JobOperationRestController jobOperationsController(){
		return new JobOperationRestController(baseConfig.jobOperator(),
				baseConfig.jobExplorer(), baseConfig.jobRegistry(), baseConfig.jobRepository(), 
				baseConfig.jobLauncher());
	}

	@Bean
	public AutomaticJobRegistrar automaticJobRegistrar() {
		AutomaticJobRegistrar automaticJobRegistrar = new AutomaticJobRegistrar();
		DefaultJobLoader defaultJobLoader = new DefaultJobLoader(jobRegistry);
		automaticJobRegistrar.setJobLoader(defaultJobLoader);
		return automaticJobRegistrar;
	}

}
