package com.ifast.batch.job.test;

import java.util.Date;

import javax.inject.Inject;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;

import com.ifast.batch.entity.test.BoonKiatTestBean;

@Configuration
public class BoonKiatTestJob {

	@Inject
	private JobBuilderFactory jobBuilderFactory;
	
	@Inject
	private StepBuilderFactory stepBuilderFactory;
	
	@Inject
    private SimpleJobLauncher jobLauncher;
	
	@Bean
	public Job job(){
		return jobBuilderFactory.get("job")
				.start(step())
				.build();
	}
	
	@Scheduled(cron = "0 0/1 * 1/1 * ?")
    public void perform() throws Exception {

        System.out.println("Job Started at :" + new Date());

        JobParameters param = new JobParametersBuilder().addString("JobID",
                String.valueOf(System.currentTimeMillis())).toJobParameters();

        JobExecution execution = jobLauncher.run(job(), param);

        System.out.println("Job finished with status :" + execution.getStatus());
    }
	
	@Bean
	public Step step(){
		return stepBuilderFactory.get("step")
				.<BoonKiatTestBean,BoonKiatTestBean>chunk(1)
				.reader(reader())
				.writer(writer())
				.build();
	}

	@Bean
	public ItemWriter<BoonKiatTestBean> writer() {
		BoonKiatTestItemWriter writer = new BoonKiatTestItemWriter();
		return writer;
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
}
