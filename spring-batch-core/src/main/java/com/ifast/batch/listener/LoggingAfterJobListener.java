package com.ifast.batch.listener;

import org.slf4j.MDC;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import com.ifast.batch.logging.DefaultJobLogFileNameCreator;
import com.ifast.batch.logging.JobLogFileNameCreator;

public class LoggingAfterJobListener implements JobExecutionListener, Ordered {

	private JobLogFileNameCreator jobLogFileNameCreator = new DefaultJobLogFileNameCreator();

	@Override
	public void beforeJob(JobExecution jobExecution) {
	}

	private void insertValuesIntoMDC(JobExecution jobExecution) {
		MDC.put(LoggingListener.JOBLOG_FILENAME, jobLogFileNameCreator.getBaseName(jobExecution));
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		insertValuesIntoMDC(jobExecution);
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Autowired(required = false)
	public void setJobLogFileNameCreator(JobLogFileNameCreator jobLogFileNameCreator) {
		this.jobLogFileNameCreator = jobLogFileNameCreator;
	}

}
