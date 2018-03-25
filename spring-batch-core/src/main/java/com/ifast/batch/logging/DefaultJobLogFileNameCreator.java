package com.ifast.batch.logging;

import org.springframework.batch.core.JobExecution;

public class DefaultJobLogFileNameCreator implements JobLogFileNameCreator {

	private final static String DEFAULT_EXTENSION = ".log";

	@Override
	public String getName(JobExecution jobExecution) {
		return getBaseName(jobExecution) + getExtension();
	}
	
	@Override
	public String getBaseName(JobExecution jobExecution) {
		return "batch-"+jobExecution.getJobInstance().getJobName()+"-"+Long.toString(jobExecution.getId());
	}
	
	@Override
	public String getExtension(){
		return DEFAULT_EXTENSION;
	}



}
