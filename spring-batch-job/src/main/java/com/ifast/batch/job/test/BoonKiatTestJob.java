package com.ifast.batch.job.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.ifast.batch.entity.test.BoonKiatTestBean;
import com.ifast.batch.partitioner.FlatFilePartitioner;
import com.ifast.batch.partitioner.MultiThreadedFlatFileItemReader;

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
				.incrementer(new RunIdIncrementer())
				.start(slaveStep())
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
	public Step slaveStep(){
		return stepBuilderFactory.get("slaveStep")
				.partitioner("step", partitioner())
				.gridSize(250)
				.step(step())
				.build();
	}
	
	@Bean 
	public Step step() {
		return stepBuilderFactory.get("step")
				.<BoonKiatTestBean,BoonKiatTestBean>chunk(250)
				.reader(reader())
				.writer(writer())
				.processor(processor())
				.build();
	}
	
	@Bean
	public BoonKiatTestItemProcessor processor() {
		return new BoonKiatTestItemProcessor();
	}

	@Bean
	public ItemWriter<BoonKiatTestBean> writer() {
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
	public FlatFileItemReader<BoonKiatTestBean> reader() {
		MultiThreadedFlatFileItemReader<BoonKiatTestBean> reader = new MultiThreadedFlatFileItemReader<BoonKiatTestBean>();
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
	public Partitioner partitioner() {
		FlatFilePartitioner filePartitioner = new FlatFilePartitioner();
		filePartitioner.setResource(new ClassPathResource("sample-data.csv"));
		return filePartitioner;
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
