package com.spring.scheduling.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.Date;

@EnableScheduling
@SpringBootApplication
@EnableBatchProcessing
public class SpringSchedulingBatchApplication {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public JobLauncher jobLauncher;

	@Autowired
	public JobExplorer jobExplorer;

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("job")
				.incrementer(new RunIdIncrementer()) // Create new ID every time the job run
				.start(step())
				.build();
	}

	@Bean
	public Step step() {
		return this.stepBuilderFactory.get("step").tasklet(((stepContribution, chunkContext) -> {
			System.out.println("The run time is: " + LocalDateTime.now());
			return RepeatStatus.FINISHED;
		})).build();
	}

	@Scheduled(cron = "0/30 * * * * *")
	public void runJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException {
		JobParametersBuilder parametersBuilder = new JobParametersBuilder();
		parametersBuilder.addDate("runTime", new Date());
		this.jobLauncher.run(job(), parametersBuilder.toJobParameters());
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringSchedulingBatchApplication.class, args);
	}

	/**
	 * Other option is to use Spring QuartzJobBean Scheduler
	 * First, extends QuartzJobBean on SpringSchedulingBatchApp class
	 * Second, implements config's methods
	 */
	/*
	@Bean
	public Trigger trigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
				.simpleSchedule()
				.withIntervalInSeconds(30)
				.repeatForever();

		return TriggerBuilder.newTrigger()
				.forJob(jobDetail())
				.withSchedule(scheduleBuilder)
				.build();
	}

	@Bean
	public JobDetail jobDetail() {
		return this.JobBuilder.newJob(SpringSchedulingBatchApplication.class)
				.storeDurably()
				.build();
	}

	@Override
	protected void executeInternal(JobExecutionContext context) {
		JobParameters jobParameters = new JobParametersBuilder(jobExplorer)
				.getNextJobParameters(job())
				.toJobParameters();

		try {
			this.jobLauncher.run(job(), jobParameters);
		} catch (JobExecutionAlreadyRunningException | JobRestartException |
				JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
			e.printStackTrace();
		}
	}*/
}
