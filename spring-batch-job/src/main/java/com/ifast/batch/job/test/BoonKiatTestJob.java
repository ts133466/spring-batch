package com.ifast.batch.job.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.ifast.batch.entity.test.BoonKiatTestBean;

@Configuration
public class BoonKiatTestJob {

	@Inject
	private JobBuilderFactory jobBuilderFactory;
	
	@Inject
	private StepBuilderFactory stepBuilderFactory;
	
	@Inject
    private SimpleJobLauncher jobLauncher;
	
	@Inject
	private DataSource dataSource;
	
	@Bean
	public Job job(){
		
		return jobBuilderFactory.get("job")
				.incrementer(new RunIdIncrementer())
				.start(step())
				.build();
	}
	
//	@Scheduled(cron = "0 0/1 * 1/1 * ?")
//    public void perform() throws Exception {
//
//        System.out.println("Job Started at :" + new Date());
//
//        JobParameters param = new JobParametersBuilder().addString("JobID",
//                String.valueOf(System.currentTimeMillis())).toJobParameters();
//
//        JobExecution execution = jobLauncher.run(job(), param);
//
//        System.out.println("Job finished with status :" + execution.getStatus());
//    }
	
	@Bean 
	public Step step() {
		return stepBuilderFactory.get("step")
				.<BoonKiatTestBean,BoonKiatTestBean>chunk(250)
				.reader(reader())
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
		JdbcBatchItemWriter<BoonKiatTestBean> writer = new JdbcBatchItemWriter<>();
		writer.setDataSource(dataSource);
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<BoonKiatTestBean>());
		writer.setSql("INSERT INTO BOON_KIAT_TEST2 VALUES (:bidPrice, :askPrice, sysdate)");
		return writer; //new BoonKiatTestItemWriter();
	}

	@Bean
	public FlatFileItemReader<BoonKiatTestBean> reader() {
		FlatFileItemReader<BoonKiatTestBean> reader = new FlatFileItemReader<BoonKiatTestBean>();
		reader.setResource(new ClassPathResource("sample-data.csv"));
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
	
	public static void main(String[] args) throws IOException {
		FileWriter fos = new FileWriter("sample-data.csv");
		PrintWriter pw = new PrintWriter(fos);
		
		for(int i = 0; i < 30000000; i++) {
			pw.println(i + "," + i);
		}
		
		pw.close();
		
	}
}
