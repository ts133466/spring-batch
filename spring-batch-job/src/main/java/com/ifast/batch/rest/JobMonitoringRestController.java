package com.ifast.batch.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.batch.operations.NoSuchJobExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ifast.batch.monitoring.RunningExecutionTracker;

@RestController
@RequestMapping("batch/monitoring")
public class JobMonitoringRestController {
	
	private static final Logger LOG = LoggerFactory.getLogger(JobMonitoringRestController.class);

	private JobOperator jobOperator;
	private JobExplorer jobExplorer;
	private RunningExecutionTracker runningExecutionTracker;

	public JobMonitoringRestController(JobOperator jobOperator, JobExplorer jobExplorer, RunningExecutionTracker runningExecutionTracker) {
		super();
		this.jobOperator = jobOperator;
		this.jobExplorer = jobExplorer;
		this.runningExecutionTracker = runningExecutionTracker;
	}

	@RequestMapping(value="/jobs", method = RequestMethod.GET)
	public Set<String> findRegisteredJobs() throws IOException {
		Set<String> registeredJobs = new HashSet<>(jobOperator.getJobNames());
		// Add JSR-352 jobs
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] xmlConfigurations = resourcePatternResolver.getResources("classpath*:/META-INF/batch-jobs/*.xml");
		for (Resource resource : xmlConfigurations) {
			registeredJobs.add(resource.getFilename().substring(0, resource.getFilename().length()-4));
		}
		return registeredJobs;
	}

	@RequestMapping(value = "/jobs/runningexecutions", method = RequestMethod.GET)
	public Set<Long> findAllRunningExecutions() {
		return runningExecutionTracker.getAllRunningExecutionIds();
	}

	@RequestMapping(value = "/jobs/runningexecutions/{jobName}", method = RequestMethod.GET)
	public Set<Long> findRunningExecutionsForJobName(@PathVariable String jobName) {
		return runningExecutionTracker.getRunningExecutionIdsForJobName(jobName);
	}

	@RequestMapping(value = "/jobs/executions/{executionId}", method = RequestMethod.GET)
	public JobExecution findExecution(@PathVariable long executionId) throws NoSuchJobExecutionException {
		JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
		if (jobExecution == null){
			throw new NoSuchJobExecutionException("JobExecution with id "+executionId+" not found.");
		}
		return jobExecution;
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(NoSuchJobExecutionException.class)
	public String handleNotFound(Exception ex) {
		LOG.warn("JobExecution not found.",ex);
		return ex.getMessage();
	}
}
