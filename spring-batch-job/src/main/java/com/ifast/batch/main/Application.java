package com.ifast.batch.main;

import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.AutomaticJobRegistrar;
import org.springframework.batch.core.configuration.support.DefaultJobLoader;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ifast.batch.config.AutomaticJobRegistrarConfiguration;
import com.ifast.batch.config.BaseConfiguration;
import com.ifast.batch.dao.DaoConfig;
import com.ifast.batch.monitoring.RunningExecutionTracker;
import com.ifast.batch.partitioner.FilePartitioner;
import com.ifast.batch.rest.JobMonitoringRestController;
import com.ifast.batch.rest.JobOperationRestController;
import com.ifast.batch.scheduler.BatchScheduler;

@SpringBootApplication
@EnableBatchProcessing
@Import({ DaoConfig.class, BatchScheduler.class, AutomaticJobRegistrarConfiguration.class, BaseConfiguration.class })
@EnableScheduling
@ComponentScan({  })
@EntityScan({ "com.ifast.batch.entity" })
@RestController
public class Application extends SpringBootServletInitializer {
	
	@Inject
	BaseConfiguration baseConfig;
	
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
	
	@RequestMapping("/")
	public String test() {
		return "TESTING 123!!";
	}
	
	@Bean
    public ResourcelessTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory(
            ResourcelessTransactionManager txManager) throws Exception {
        
        MapJobRepositoryFactoryBean factory = new 
                MapJobRepositoryFactoryBean(txManager);
        
        factory.afterPropertiesSet();
        
        return factory;
    }

    @Bean
    public JobRepository jobRepository(
            MapJobRepositoryFactoryBean factory) throws Exception {
        return factory.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
    
    @Bean
    public RunningExecutionTracker runningExecutionTracker() {
    	return new RunningExecutionTracker();
    }
    
	@Bean
	public JobMonitoringRestController jobMonitoringController(){
		return new JobMonitoringRestController(baseConfig.jobOperator(), 
				baseConfig.jobExplorer(), runningExecutionTracker());
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
	
	@Bean
	public FilePartitioner filePartitioner() {
		FilePartitioner filePartitioner = new FilePartitioner();
		filePartitioner.setOutputPath("/opt/bea/chunk");
		return filePartitioner;
	}
}
