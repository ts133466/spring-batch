package com.ifast.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class SimpleJobExecutionListener implements JobExecutionListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(SimpleJobExecutionListener.class);

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// can send email here
		LOG.info(String.format("%s has started on %s", jobExecution.getJobInstance().getJobName(), jobExecution.getCreateTime()));
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		// can send email here
		long time = ((jobExecution.getEndTime().getTime() - jobExecution.getCreateTime().getTime()) / 1000);
		LOG.info(Long.toString(time));
		LOG.info(String.format("%s has started on %s has ended on %s with status %s. Total time used: %s min %s secs", 
				jobExecution.getJobInstance().getJobName(),
				jobExecution.getCreateTime(),
				jobExecution.getEndTime(),
				jobExecution.getExitStatus().getExitCode(),
				time / 60,
				time % 60));
	}

}
