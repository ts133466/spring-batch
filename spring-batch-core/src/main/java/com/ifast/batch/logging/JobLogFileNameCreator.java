package com.ifast.batch.logging;

import org.springframework.batch.core.JobExecution;

public interface JobLogFileNameCreator {

	public String getName(JobExecution jobExecution);

	public String getBaseName(JobExecution jobExecution);

	public String getExtension();

}
