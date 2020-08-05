package com.linkedin.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class LinkedinBatchApplication {

	// To launch the job with parameters run on target's folder:
	// java -jar linkedin-batch-02-02-0.0.1-SNAPSHOT.jar "item=shoes" "run.date(date)=2020/01/01"

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job deliverPackageJob() {
		return this.jobBuilderFactory.get("initDeliveryPackage")
				.start(deliverPackageStep())
				.next(driveToAddressStep())
					.on("FAILED").fail() // or we can use ".stop()" to enter on STOPPED status
				.from(driveToAddressStep())
					.on("*").to(decider())
						.on("PRESENT").to(givePackageToCustomerStep())
							.next(customerItemDecider()).on("CORRECT").to(thanksTheCustomerStep())
							.from(customerItemDecider()).on("WRONG").to(refundTheCustomerStep())
					.from(decider())
						.on("NOT_PRESENT").to(leaveAtDoorStep())
				.end()
				.build();
	}

	@Bean
	public Step deliverPackageStep() {
		return this.stepBuilderFactory.get("deliverPackage").tasklet((stepContribution, chunkContext) -> {
			String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
			String date = chunkContext.getStepContext().getJobParameters().get("run.date").toString();

			System.out.println(String.format("The %s has been delivered on %s.", item, date));
			return RepeatStatus.FINISHED;
		}).build();
	}

	@Bean
	public Step driveToAddressStep() {
		boolean GOT_LOST = true;

		return this.stepBuilderFactory.get("driveToAddress").tasklet(((stepContribution, chunkContext) -> {
			if (GOT_LOST) {
				throw new RuntimeException(("Got lost driving to the address"));
			}

			System.out.println("Successfully arrived at the address!");
			return RepeatStatus.FINISHED;
		})).build();
	}

	@Bean
	public Step storePackageStep() {
		return this.stepBuilderFactory.get("storePackage").tasklet(((stepContribution, chunkContext) -> {
			System.out.println("Storing the package while the customer address is located.");
			return RepeatStatus.FINISHED;
		})).build();
	}

	@Bean
	public JobExecutionDecider decider() {
		return new DeliveryDecider();
	}

	@Bean
	public Step givePackageToCustomerStep() {
		return this.stepBuilderFactory.get("givePackageToCustomer").tasklet(((stepContribution, chunkContext) -> {
			System.out.println("Given the package to the customer.");
			return RepeatStatus.FINISHED;
		})).build();
	}

	@Bean
	public JobExecutionDecider customerItemDecider() {
		return new CustomerItemDecider();
	}

	@Bean
	public Step thanksTheCustomerStep() {
		return this.stepBuilderFactory.get("thanksTheCustomer").tasklet(((stepContribution, chunkContext) -> {
			System.out.println("Thanks for the customer request!");
			return RepeatStatus.FINISHED;
		})).build();
	}

	@Bean
	public Step refundTheCustomerStep() {
		return this.stepBuilderFactory.get("refundTheCustomer").tasklet(((stepContribution, chunkContext) -> {
			System.out.println("Sorry, you'll get a refund!");
			return RepeatStatus.FINISHED;
		})).build();
	}

	@Bean
	public Step leaveAtDoorStep() {
		return this.stepBuilderFactory.get("leaveAtDoor").tasklet(((stepContribution, chunkContext) -> {
			System.out.println("Leaving the package at the door.");
			return RepeatStatus.FINISHED;
		})).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(LinkedinBatchApplication.class, args);
	}
}
