package com.spring.demo.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

import static com.spring.demo.batch.Constants.*;

@SpringBootApplication
@EnableBatchProcessing
public class BatchDemoApplication {

	// java -jar spring-batch-demo-0.0.1-SNAPSHOT.jar

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	// Spring.DataSource DB configs on application.properties
	@Autowired
	private DataSource dataSource;

	int chunkSize = 10;

	@Bean
	public Job job() throws Exception {
		return this.jobBuilderFactory.get("job")
				.start(chunkBasedStep())
				.build();
	}

	@Bean
	public Step chunkBasedStep() throws Exception {
		return this.stepBuilderFactory.get("chunkBasedStep")
				.<Order, TrackedOrder>chunk(chunkSize) // <Input, Output>chunk
				.reader(itemReader_DB())
				.processor(compositeItemProcessor())
				.faultTolerant()
				.retry(OrderProcessingException.class)
				.retryLimit(3) // Only for the particular item e not the entire step
				.listener(new CustomRetryListener())
				//.skip(OrderProcessingException.class) // If throws OrderProcessingException the batch will continue normally
				//.skipLimit(5) // How many exception to stop the batch
				//.listener(new CustomSkipListener())
				.writer(itemWriter_DB())
				.taskExecutor(taskExecutor()) // Multi thread job
				.build();
	}

	/** ==================================================================================
	 *
	 *                    MultiThread Step executor (Cannot restart job)
	 *
	 * ==================================================================================
	 */
	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(10);
		return executor;
	}

	/** ==================================================================================
	 *
	 *                    				Processors
	 *
	 * ==================================================================================
	 */
	@Bean
	public ItemProcessor<Order,Order> orderValidatingItemProcessor() {
		BeanValidatingItemProcessor<Order> itemProcessor = new BeanValidatingItemProcessor<>();
		itemProcessor.setFilter(true);
		return itemProcessor;
	}

	@Bean
	public ItemProcessor<Order, TrackedOrder> trackedOrderItemProcessor() {
		return new TrackedOrderItemProcessor();
	}

	@Bean
	public ItemProcessor<TrackedOrder, TrackedOrder> freeShippingOrderItemProcessor() {
		return new FreeShippingOrderItemProcessor();
	}

	@Bean
	public ItemProcessor<Order,TrackedOrder> compositeItemProcessor() {
		return new CompositeItemProcessorBuilder<Order, TrackedOrder>()
				.delegates(
						orderValidatingItemProcessor(),
						trackedOrderItemProcessor(),
						freeShippingOrderItemProcessor()
				)
				.build();
	}

	/** ==================================================================================
	 *
	 *                    Reader and Writer Methods for DB
	 *
	 * ==================================================================================
	 */
	// JdbcCursorItemReader is not thread safe and should not be used in multi thread scenarios
	// JdbcPagingItemReader is thread safe
	@Bean
	public ItemReader<Order> itemReader_DB() throws Exception {
		return new JdbcPagingItemReaderBuilder<Order>()
				.dataSource(dataSource)
				.name("jdbcCursorItemReader")
				.queryProvider(queryProvider())
				.rowMapper(new OrderRowMapper())
				.pageSize(10) // Read this amount of item on DB, must be equal to the chunkSize
				.saveState(false) // For multithreaded process
				.build();
	}

	@Bean
	public PagingQueryProvider queryProvider() throws Exception {
		SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

		factoryBean.setSelectClause("select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
		factoryBean.setFromClause("from SHIPPED_ORDER");
		factoryBean.setSortKey("order_id");
		factoryBean.setDataSource(dataSource);

		return factoryBean.getObject();
	}

	@Bean
	public ItemWriter<TrackedOrder> itemWriter_DB() {
		return new JdbcBatchItemWriterBuilder<TrackedOrder>()
				.dataSource(dataSource)
				.sql(INSERT_TRACKED_SQL)
				.beanMapped()
				.build();
	}

	/** ==================================================================================
	 *
	 *                    Reader and Writer Methods for CSV files
	 *
	 * ==================================================================================
	 */
	@Bean
	public ItemReader<Order> itemReader_CSV() {
		FlatFileItemReader<Order> itemReader = new FlatFileItemReader<>();
		itemReader.setLinesToSkip(1); // Skip Headers to read
		itemReader.setResource(new FileSystemResource("src/main/resources/MOCK_DATA.csv"));

		DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<>(); // Parse different lines of the csv data
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(); // Break the line by ","

		tokenizer.setNames(CSV_HEADERS);
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(new OrderFieldSetMapper());

		itemReader.setLineMapper(lineMapper);
		return itemReader;
	}

	@Bean
	public ItemWriter<Order> itemWriter_CSV() {
		FlatFileItemWriter<Order> itemWriter = new FlatFileItemWriter<>();
		itemWriter.setResource(new FileSystemResource("src/main/resources/shipped_orders_output.csv"));

		DelimitedLineAggregator<Order> aggregator = new DelimitedLineAggregator<>();
		aggregator.setDelimiter(",");

		BeanWrapperFieldExtractor<Order> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(ORDER_FIELDS);
		aggregator.setFieldExtractor(fieldExtractor);

		itemWriter.setLineAggregator(aggregator);
		return itemWriter;
	}

	/** ==================================================================================
	 *
	 *                    Writer Method for creating a JSON file
	 *
	 * ==================================================================================
	 */
	@Bean
	public ItemWriter<TrackedOrder> itemWriter_JSON() {
		return new JsonFileItemWriterBuilder<TrackedOrder>()
				.jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
				.resource(new FileSystemResource("src/main/resources/shipped_orders_output.json"))
				.name("jsonFileItemWriter")
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(BatchDemoApplication.class, args);
	}
}
