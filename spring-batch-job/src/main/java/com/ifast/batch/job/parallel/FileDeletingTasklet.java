package com.ifast.batch.job.parallel;
import java.io.File;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


public class FileDeletingTasklet implements Tasklet, InitializingBean {

	private File file;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		 Assert.notNull(file, "file must be set");
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        Assert.state(file.isFile(), "[Assertion failed] - this state invariant must be true");
        
        file.delete();
        
        System.out.println("DONE 2");
        
        return RepeatStatus.FINISHED;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
