package com.ifast.batch.job.parallel;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class FileCopyTasklet implements Tasklet, InitializingBean {
	
	private File file;
	
	private String destination;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(destination, "directory must be set");
		Assert.notNull(file, "file must be set");
		
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Assert.state(file.isFile(), "[Assertion failed] - this state invariant must be true");
        
       File directory = new File(destination);
       boolean success = directory.mkdir();
	   FileUtils.copyFileToDirectory(file, directory);
	   
	   System.out.println("DONE 1");
        
		return RepeatStatus.FINISHED;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

}
