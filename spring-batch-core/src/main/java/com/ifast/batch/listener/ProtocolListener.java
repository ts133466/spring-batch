package com.ifast.batch.listener;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.core.Ordered;

public class ProtocolListener implements JobExecutionListener, Ordered {

	private static final int DEFAULT_WIDTH = 80;
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolListener.class);

	public void afterJob(JobExecution jobExecution) {
		StringBuilder protocol = new StringBuilder();
		protocol.append("\n");
		protocol.append(createFilledLine('*'));
		protocol.append(createFilledLine('-'));
		protocol.append("Protocol for " + jobExecution.getJobInstance().getJobName() + " \n");
		protocol.append("  Started:      " + jobExecution.getStartTime() + "\n");
		protocol.append("  Finished:     " + jobExecution.getEndTime() + "\n");
		protocol.append("  Exit-Code:    " + jobExecution.getExitStatus().getExitCode() + "\n");
		protocol.append("  Exit-Descr:   " + jobExecution.getExitStatus().getExitDescription() + "\n");
		protocol.append("  Status:       " + jobExecution.getStatus() + "\n");
		protocol.append("  Content of Job-ExecutionContext:\n");
		for (Entry<String, Object> entry : jobExecution.getExecutionContext().entrySet()) {
			protocol.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
		}
		protocol.append("  Job-Parameter: \n");
		JobParameters jp = jobExecution.getJobParameters();
		for (Iterator<Entry<String, JobParameter>> iter = jp.getParameters().entrySet().iterator(); iter.hasNext();) {
			Entry<String, JobParameter> entry = iter.next();
			protocol.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
		}
		protocol.append(createFilledLine('-'));
		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
			protocol.append("Step " + stepExecution.getStepName() + " \n");
			protocol.append("  ReadCount:    " + stepExecution.getReadCount() + "\n");
			protocol.append("  WriteCount:   " + stepExecution.getWriteCount() + "\n");
			protocol.append("  Commits:      " + stepExecution.getCommitCount() + "\n");
			protocol.append("  SkipCount:    " + stepExecution.getSkipCount() + "\n");
			protocol.append("  Rollbacks:    " + stepExecution.getRollbackCount() + "\n");
			protocol.append("  Filter:       " + stepExecution.getFilterCount() + "\n");
			protocol.append("  Content of Step-ExecutionContext:\n");
			for (Entry<String, Object> entry : stepExecution.getExecutionContext().entrySet()) {
				protocol.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
			}
			protocol.append(createFilledLine('-'));
		}
		protocol.append(createFilledLine('*'));
		LOGGER.info(protocol.toString());
	}

	public void beforeJob(JobExecution jobExecution) {
		StringBuilder protocol = new StringBuilder();
		protocol.append(createFilledLine('-'));
		protocol.append("Job " + jobExecution.getJobInstance().getJobName() + " started with Job-Execution-Id " + jobExecution.getId() + " \n");
		protocol.append("Job-Parameter: \n");
		JobParameters jp = jobExecution.getJobParameters();
		for (Iterator<Entry<String, JobParameter>> iter = jp.getParameters().entrySet().iterator(); iter.hasNext();) {
			Entry<String, JobParameter> entry = iter.next();
			protocol.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
		}
		protocol.append(createFilledLine('-'));
		LOGGER.info(protocol.toString());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10;
	}

	/**
	 * Create line with defined char
	 * 
	 * @param filler
	 */
	private String createFilledLine(char filler) {
		return StringUtils.leftPad("", DEFAULT_WIDTH, filler) + "\n";
	}

}
