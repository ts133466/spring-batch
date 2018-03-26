package com.ifast.batch.listener;

import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

import com.ifast.batch.logging.DefaultJobLogFileNameCreator;
import com.ifast.batch.logging.JobLogFileNameCreator;

public class LoggingListener implements JobExecutionListener, StepExecutionListener, Ordered {
	
	private JobLogFileNameCreator jobLogFileNameCreator = new DefaultJobLogFileNameCreator();

	public static final String JOBLOG_FILENAME = "jobLogFileName";

	@Override
	public void beforeJob(JobExecution jobExecution) {
		insertValuesIntoMDC(jobExecution);
	}

	private void insertValuesIntoMDC(JobExecution jobExecution) {
		MDC.put(JOBLOG_FILENAME, jobLogFileNameCreator.getBaseName(jobExecution));
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		removeValuesFromMDC();
	}

	private void removeValuesFromMDC() {
		MDC.remove(JOBLOG_FILENAME);
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		insertValuesIntoMDC(stepExecution.getJobExecution());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		removeValuesFromMDC();
		return null;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Autowired(required=false)
	public void setJobLogFileNameCreator(JobLogFileNameCreator jobLogFileNameCreator) {
		this.jobLogFileNameCreator = jobLogFileNameCreator;
	}

}
