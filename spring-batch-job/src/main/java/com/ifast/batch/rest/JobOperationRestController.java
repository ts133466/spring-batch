package com.ifast.batch.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.batch.operations.JobExecutionAlreadyCompleteException;
import javax.batch.operations.JobStartException;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.converter.JobParametersConverter;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.support.PropertiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ifast.batch.logging.DefaultJobLogFileNameCreator;
import com.ifast.batch.logging.JobLogFileNameCreator;

@RestController
@RequestMapping("batch/operations")
public class JobOperationRestController {

	private static final Logger LOG = LoggerFactory.getLogger(JobOperationRestController.class);

	public static final String JOB_PARAMETERS = "jobParameters";

	private JobOperator jobOperator;
	private JobExplorer jobExplorer;
	private JobRegistry jobRegistry;
	private JobRepository jobRepository;
	private JobLauncher jobLauncher;
	private JobParametersConverter jobParametersConverter = new DefaultJobParametersConverter();
	private JobLogFileNameCreator jobLogFileNameCreator = new DefaultJobLogFileNameCreator();

	public JobOperationRestController(JobOperator jobOperator,
			JobExplorer jobExplorer, JobRegistry jobRegistry,
			JobRepository jobRepository, JobLauncher jobLauncher) {
		super();
		this.jobOperator = jobOperator;
		this.jobExplorer = jobExplorer;
		this.jobRegistry = jobRegistry;
		this.jobRepository = jobRepository;
		this.jobLauncher = jobLauncher;
	}

	@RequestMapping(value = "/jobs/{jobName}", method = RequestMethod.GET)
	public String launch(@PathVariable String jobName, @RequestParam MultiValueMap<String, String> payload)	throws NoSuchJobException, JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException,	JobInstanceAlreadyCompleteException, JobParametersNotFoundException {
		String parameters = payload.getFirst(JOB_PARAMETERS);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Attempt to start job with name " + jobName + " and parameters "+parameters+".");
		}
		try {
			Job job = jobRegistry.getJob(jobName);
			JobParameters jobParameters = createJobParametersWithIncrementerIfAvailable(parameters, job);
			Long id = jobLauncher.run(job, jobParameters).getId();
			return String.valueOf(id);
		} catch (NoSuchJobException e){
			throw e;
		}
	}

	private JobParameters createJobParametersWithIncrementerIfAvailable(String parameters, Job job) throws JobParametersNotFoundException {
		JobParameters jobParameters = jobParametersConverter.getJobParameters(PropertiesConverter.stringToProperties(parameters));
		// use JobParametersIncrementer to create JobParameters if incrementer is set and only if the job is no restart
		if (job.getJobParametersIncrementer() != null){
			JobExecution lastJobExecution = jobRepository.getLastJobExecution(job.getName(), jobParameters);
			boolean restart = false;
			// check if job failed before
			if (lastJobExecution != null) {
				BatchStatus status = lastJobExecution.getStatus();
				if (status.isUnsuccessful() && status != BatchStatus.ABANDONED) {
					restart = true;
				}
			}
			// if it's not a restart, create new JobParameters with the incrementer
			if (!restart) {
				JobParameters nextParameters = getNextJobParameters(job);
				Map<String, JobParameter> map = new HashMap<String, JobParameter>(nextParameters.getParameters());
				map.putAll(jobParameters.getParameters());
				jobParameters = new JobParameters(map);
			}
		}
		return jobParameters;
	}

	/**
	 * Borrowed from CommandLineJobRunner.
	 * @param job the job that we need to find the next parameters for
	 * @return the next job parameters if they can be located
	 * @throws JobParametersNotFoundException if there is a problem
	 */
	private JobParameters getNextJobParameters(Job job) throws JobParametersNotFoundException {
		String jobIdentifier = job.getName();
		JobParameters jobParameters;
		List<JobInstance> lastInstances = jobExplorer.getJobInstances(jobIdentifier, 0, 1);

		JobParametersIncrementer incrementer = job.getJobParametersIncrementer();

		if (lastInstances.isEmpty()) {
			jobParameters = incrementer.getNext(new JobParameters());
			if (jobParameters == null) {
				throw new JobParametersNotFoundException("No bootstrap parameters found from incrementer for job="
						+ jobIdentifier);
			}
		}
		else {
			List<JobExecution> lastExecutions = jobExplorer.getJobExecutions(lastInstances.get(0));
			jobParameters = incrementer.getNext(lastExecutions.get(0).getJobParameters());
		}
		return jobParameters;
	}


	@RequestMapping(value = "/jobs/executions/{executionId}", method = RequestMethod.GET)
	public String getStatus(@PathVariable long executionId) throws NoSuchJobExecutionException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Get ExitCode for JobExecution with id: " + executionId+".");
		}
		JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
		if (jobExecution != null){
			return jobExecution.getExitStatus().getExitCode();
		} else {
			throw new NoSuchJobExecutionException("JobExecution with id "+executionId+" not found.");
		}
	}

	@RequestMapping(value = "/jobs/executions/{executionId}/log", method = RequestMethod.GET)
	public void getLogFile(HttpServletResponse response, @PathVariable long executionId) throws NoSuchJobExecutionException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Get log file for job with executionId: " + executionId);
		}
		String loggingPath = createLoggingPath();
		JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
		if (jobExecution == null){
			throw new NoSuchJobExecutionException("JobExecution with id "+executionId+" not found.");
		}
		File downloadFile = new File(loggingPath+jobLogFileNameCreator.getName(jobExecution));
		InputStream is = new FileInputStream(downloadFile);
		FileCopyUtils.copy(is, response.getOutputStream());
		response.flushBuffer();
	}

	private String createLoggingPath() {
		String loggingPath = System.getProperty("LOG_PATH");
		if (loggingPath == null){
			loggingPath = System.getProperty("java.io.tmpdir");
		}
		if (loggingPath == null){
			loggingPath = "/tmp";
		}
		if (!loggingPath.endsWith("/")){
			loggingPath = loggingPath+"/";
		}
		return loggingPath;
	}

	@RequestMapping(value = "/jobs/executions/{executionId}", method = RequestMethod.DELETE)
	public String stop(@PathVariable long executionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Stop JobExecution with id: " + executionId);
		}
		Boolean successful = jobOperator.stop(executionId);
		return successful.toString();
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({NoSuchJobException.class, NoSuchJobExecutionException.class, JobStartException.class})
	public String handleNotFound(Exception ex) {
		LOG.warn("Job or JobExecution not found.",ex);
		return ex.getMessage();
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(JobParametersNotFoundException.class)
	public String handleNoBootstrapParametersCreatedByIncrementer(Exception ex) {
		LOG.warn("JobParametersIncrementer didn't provide bootstrap parameters.",ex);
		return ex.getMessage();
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler({UnexpectedJobExecutionException.class, JobInstanceAlreadyExistsException.class, JobInstanceAlreadyCompleteException.class})
	public String handleAlreadyExists(Exception ex) {
		LOG.warn("JobInstance or JobExecution already exists.",ex);
		return ex.getMessage();
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler({JobExecutionAlreadyRunningException.class, JobExecutionAlreadyCompleteException.class, JobRestartException.class})
	public String handleAlreadyRunningOrComplete(Exception ex) {
		LOG.warn("JobExecution already running or complete.",ex);
		return ex.getMessage();
	}

	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	@ExceptionHandler(JobParametersInvalidException.class)
	public String handleParametersInvalid(Exception ex) {
		LOG.warn("Job parameters are invalid.",ex);
		return ex.getMessage();
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler(FileNotFoundException.class)
	public String handleFileNotFound(Exception ex) {
		LOG.warn("Logfile not found.",ex);
		return ex.getMessage();
	}

	@ResponseStatus(HttpStatus.CONFLICT)
	@ExceptionHandler(JobExecutionNotRunningException.class)
	public String handleNotRunning(Exception ex) {
		LOG.warn("JobExecution is not running.",ex);
		return ex.getMessage();
	}

	@Autowired(required=false)
	public void setJobLogFileNameCreator(JobLogFileNameCreator jobLogFileNameCreator) {
		this.jobLogFileNameCreator = jobLogFileNameCreator;
	}
}
