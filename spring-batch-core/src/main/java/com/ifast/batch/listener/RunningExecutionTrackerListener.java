package com.ifast.batch.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import com.ifast.batch.monitoring.RunningExecutionTracker;

public class RunningExecutionTrackerListener implements JobExecutionListener {
	
	private RunningExecutionTracker runningExecutionTracker;
	
	public RunningExecutionTrackerListener(
			RunningExecutionTracker runningExecutionTracker) {
		super();
		this.runningExecutionTracker = runningExecutionTracker;
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		runningExecutionTracker.addRunningExecution(jobExecution.getJobInstance().getJobName(), jobExecution.getId());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		runningExecutionTracker.removeRunningExecution(jobExecution.getId());
	}

}
