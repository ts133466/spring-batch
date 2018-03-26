package com.ifast.batch.config;

import javax.annotation.Resource;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.job.AbstractJob;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import com.ifast.batch.listener.ListenerService;
import com.ifast.batch.listener.LoggingAfterJobListener;
import com.ifast.batch.listener.LoggingListener;
import com.ifast.batch.listener.ProtocolListener;
import com.ifast.batch.listener.RunningExecutionTrackerListener;
import com.ifast.batch.monitoring.RunningExecutionTracker;

@Configuration
@ComponentScan({ "com.ifast.batch.listener", "com.ifast.batch.logging", 
				 "com.ifast.batch.monitoring", "com.ifast.batch.scheduler" })
@Import({ AutomaticJobRegistrarConfiguration.class })
public class CoreConfig extends DefaultBatchConfigurer implements ApplicationListener<ContextRefreshedEvent>, Ordered {
	
	@Resource
	private Environment env;

	@Bean
	public LoggingListener loggingListener() {
		return new LoggingListener();
	}

	@Bean
	public LoggingAfterJobListener loggingAfterJobListener() {
		return new LoggingAfterJobListener();
	}

	@Bean
	public ProtocolListener protocolListener() {
		return new ProtocolListener();
	}

	@Bean
	public RunningExecutionTracker runningExecutionTracker() {
		return new RunningExecutionTracker();
	}

	@Bean
	public RunningExecutionTrackerListener runningExecutionTrackerListener() {
		return new RunningExecutionTrackerListener(runningExecutionTracker());
	}
	
	@Bean
	public ListenerService addListenerToJobService() {
		boolean addProtocolListener = env.getProperty("batch.defaultprotocol.enabled", boolean.class, true);
		boolean addLoggingListener = env.getProperty("batch.logfileseparation.enabled", boolean.class, true);
		return new ListenerService(addProtocolListener, addLoggingListener, protocolListener(), runningExecutionTrackerListener(),
				loggingListener(), loggingAfterJobListener());
	}
	
	@Bean
	public BaseConfiguration baseConfiguration() {
		return new BaseConfiguration();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			for (String jobName : baseConfiguration().jobRegistry().getJobNames()) {
				AbstractJob job = (AbstractJob) baseConfiguration().jobRegistry().getJob(jobName);
				this.addListenerToJobService().addListenerToJob(job);
			}
		} catch (NoSuchJobException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

    @Bean
    public SimpleJobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }
	
}
