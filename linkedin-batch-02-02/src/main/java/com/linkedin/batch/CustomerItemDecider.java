package com.linkedin.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class CustomerItemDecider implements JobExecutionDecider {

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        String result = Math.random() <= 0.7 ? "CORRECT" : "WRONG";
        System.out.println("Decider result is: " + result);
        return new FlowExecutionStatus(result);
    }
}
