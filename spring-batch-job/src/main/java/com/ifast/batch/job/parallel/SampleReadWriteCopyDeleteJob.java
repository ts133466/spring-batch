package com.ifast.batch.job.parallel;

import java.io.File;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.file.Files;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;

import com.ifast.batch.entity.test.BoonKiatTestBean;
import com.ifast.batch.integration.FileMessageToJobRequest;
import com.ifast.batch.job.test.BoonKiatTestItemProcessor;

@Configuration
public class SampleReadWriteCopyDeleteJob {
	
	@Inject
	private JobBuilderFactory jobBuilderFactory;

	@Inject
	private StepBuilderFactory stepBuilderFactory;

	@Inject
	private DataSource dataSource;
	
	@Inject
	private JobRepository jobRepository;

	@Bean
	public Job sampleFlowJob(){

		return jobBuilderFactory.get("sampleFlowJob")
				.incrementer(new RunIdIncrementer())
				.start(step1())
				.next(step3())
				.next(step4()).on("COMPLETED").to(step5())
				.next(step6())
				.end()
				.build();
	}
	
	@Bean
	@StepScope
	public FileCopyTasklet fileCopyTasklet(@Value("#{jobParameters[inputFile]}") String fileName) {
		FileCopyTasklet fileCopyTasklet = new FileCopyTasklet();
		fileCopyTasklet.setDestination("/opt/bea/validated/");
		fileCopyTasklet.setFile(new File(fileName));
		return fileCopyTasklet;
	}
	
	@Bean
	@StepScope
	public FileDeletingTasklet fileDeletingTasklet(@Value("#{jobParameters[inputFile]}") String fileName) {
		FileDeletingTasklet tasklet = new FileDeletingTasklet();

        tasklet.setFile(new File(fileName));

        return tasklet;
	}
	
	@Bean
	public Tasklet fileDelete(String fileName) {
		FileDeletingTasklet tasklet = new FileDeletingTasklet();

        tasklet.setFile(new File(fileName));

        return tasklet;
	}
	
	@Bean
	public Step step4() {
		return stepBuilderFactory.get("step4")
				.tasklet(fileCopyTasklet(null))
				.build();
	}
	
	@Bean
	public Step step5() {
		return stepBuilderFactory.get("step5")
				.tasklet(fileDeletingTasklet(null))
				.build();
	}
	
	@Bean
	public Step step6() {
		return stepBuilderFactory.get("step6")
				.tasklet(fileDelete("/opt/bea/test.csv"))
				.build();
	}

	@Bean 
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<BoonKiatTestBean,BoonKiatTestBean>chunk(250)
				.reader(reader((null)))
				.writer(fileWriter())
				.build();
	}
	
	@Bean
	public Step step3() {
		return stepBuilderFactory.get("step3")
				.<BoonKiatTestBean,BoonKiatTestBean>chunk(250)
				.reader(reader(("/opt/bea/test.csv")))
				.writer(writer())
				.processor(processor())
				.taskExecutor(taskExecutor())
				.build();
	}

	@Bean
	public BoonKiatTestItemProcessor processor() {
		return new BoonKiatTestItemProcessor();
	}

	@Bean
	public ItemWriter<BoonKiatTestBean> writer() {
//		HibernateItemWriter<BoonKiatTestBean> writer = new HibernateItemWriter<>();
//		writer.setSessionFactory(sessionFactory);
		JdbcBatchItemWriter<BoonKiatTestBean> writer = new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<BoonKiatTestBean>());
		writer.setSql("INSERT INTO BOON_KIAT_TEST2 VALUES (:bidPrice, :askPrice, sysdate)");
		return writer; 
	}
	
	@Bean
	public ItemWriter<BoonKiatTestBean> fileWriter() {
		FlatFileItemWriter<BoonKiatTestBean> writer = new FlatFileItemWriter<BoonKiatTestBean>();
		writer.setResource(new FileSystemResource("/opt/bea/test.csv"));
		writer.setLineAggregator(new DelimitedLineAggregator<BoonKiatTestBean>() {{
			setFieldExtractor(new BeanWrapperFieldExtractor<BoonKiatTestBean>() {{
				setNames(new String[] { "bidPrice", "askPrice" });
			}});
		}});
		writer.setAppendAllowed(true);
		return writer;
	}

	@Bean
	@StepScope
	public FlatFileItemReader<BoonKiatTestBean> reader(@Value("#{jobParameters[inputFile]}") String fileName) {
		FlatFileItemReader<BoonKiatTestBean> reader = new FlatFileItemReader<BoonKiatTestBean>();
		reader.setResource(new FileSystemResource(fileName));
		reader.setLineMapper(new DefaultLineMapper<BoonKiatTestBean>() {{
			setLineTokenizer(new DelimitedLineTokenizer() {{
				setNames(new String[] { "bidPrice", "askPrice" });
			}});
			setFieldSetMapper(new BeanWrapperFieldSetMapper<BoonKiatTestBean>() {{
				setTargetType(BoonKiatTestBean.class);
			}});
		}});
		return reader;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(3);
		return taskExecutor;
	}
	
	@Bean
	public FileMessageToJobRequest fileMessageToJobRequest() {
	    FileMessageToJobRequest fileMessageToJobRequest = new FileMessageToJobRequest();
	    fileMessageToJobRequest.setFileParameterName("inputFile");
	    fileMessageToJobRequest.setJob(sampleFlowJob());
	    return fileMessageToJobRequest;
	}

	@Bean
	public JobLaunchingGateway jobLaunchingGateway() {
	    SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
	    simpleJobLauncher.setJobRepository(jobRepository);
	    simpleJobLauncher.setTaskExecutor(new SyncTaskExecutor());
	    JobLaunchingGateway jobLaunchingGateway = new JobLaunchingGateway(simpleJobLauncher);

	    return jobLaunchingGateway;
	}

	@Bean
	public IntegrationFlow integrationFlow(JobLaunchingGateway jobLaunchingGateway) {
	    return IntegrationFlows.from(Files.inboundAdapter(new File("C:/opt/bea/tmp/"))
	    		.filter(new CompositeFileListFilter<File>() {{
	    			addFilter(new SimplePatternFileListFilter("*.csv"));
	    			addFilter(new AcceptOnceFileListFilter<File>());
	    		}}), 
	    		new Consumer<SourcePollingChannelAdapterSpec>() {
					
					@Override
					public void accept(SourcePollingChannelAdapterSpec c) {
						c
						.poller(Pollers.fixedDelay(1000));
					}
				})
                .handle(fileMessageToJobRequest())
	            .handle(jobLaunchingGateway)
	            .channel("nullChannel")
	            .get();
	}
}
