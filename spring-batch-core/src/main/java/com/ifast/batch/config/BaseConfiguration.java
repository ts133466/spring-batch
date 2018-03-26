package com.ifast.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseConfiguration {

	// Created by spring-boot-starter-batch in combination with our TaskExecutorBatchConfigurer
	@Autowired
	private JobOperator jobOperator;
	
	@Autowired
	private JobExplorer jobExplorer;
	
	@Autowired
	private JobRegistry jobRegistry;
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private DataSource dataSource;

	public JobOperator jobOperator() {
		return jobOperator;
	}
	public JobExplorer jobExplorer() {
		return jobExplorer;
	}
	public JobRegistry jobRegistry() {
		return jobRegistry;
	}
	public JobRepository jobRepository() {
		return jobRepository;
	}
	public JobLauncher jobLauncher() {
		return jobLauncher;
	}
	public DataSource dataSource() {
		return dataSource;
	}
}
