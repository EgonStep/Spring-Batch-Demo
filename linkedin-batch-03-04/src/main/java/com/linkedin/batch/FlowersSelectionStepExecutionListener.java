package com.linkedin.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.util.StringUtils;

public class FlowersSelectionStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Executing before step logic!");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("Executing after step logic!");
        String flowerType = stepExecution.getJobParameters().getString("type");

        if (StringUtils.isEmpty(flowerType))
            throw new RuntimeException("No type added");

        return flowerType.equalsIgnoreCase("roses") ? new ExitStatus("TRIM REQUIRED") :
                new ExitStatus("NO TRIM REQUIRED");
    }
}
