package BasePack.Config;

import BasePack.Model.CustomerOrderSummary;
import BasePack.Model.CustomerOrders;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
public class BatchDatabaseToCSV {

    @Autowired
    public DataSource dataSource;

//    @Autowired
//    public SqlPagingQueryProviderFactoryBean queryProviderFactoryBean;

    @Bean
    public Job jobCreateCustomer_RFM_CSV(TaskExecutor taskExecutor, JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new JobBuilder("jobPrepareCustomer_RFM_CSV", jobRepository)
                .start(csvStep(taskExecutor,jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step csvStep(TaskExecutor taskExecutor, JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        return new StepBuilder("csvStep", jobRepository)
                .<CustomerOrders, CustomerOrderSummary>chunk(10, transactionManager)
                .reader(orderItemReader())
                .processor(orderProcessor())
                .writer(csvWriter())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public ItemReader<CustomerOrders> orderItemReader() throws Exception {

        return new JdbcPagingItemReaderBuilder<CustomerOrders>()
            .name("customerReader")
            .dataSource(dataSource)
            .queryProvider(Objects.requireNonNull(queryProviderFactoryBean().getObject()))
            //.parameterValues(parameterValues)
            .rowMapper(new BeanPropertyRowMapper<>(CustomerOrders.class))
            .pageSize(50)
            .build();
    }

    @Bean
    public SqlPagingQueryProviderFactoryBean queryProviderFactoryBean() {
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
        provider.setDataSource(dataSource);
        provider.setSelectClause("SELECT customer_id, total_amount, totalOrders, lastOrderDate");
        provider.setFromClause("from orders");
        //provider.setGroupClause("GROUP BY customer_id");
        provider.setWhereClause("WHERE order_date = CURDATE()");
        provider.setSortKey("id");
        return provider;
    }

    @Bean
    public ItemProcessor<CustomerOrders, CustomerOrderSummary> orderProcessor() {
        return Customer_order -> new CustomerOrderSummary(
                Customer_order.getCustomerId(),
                Customer_order.getTotalSpend(),
                Customer_order.getTotalOrders(),
                Customer_order.getLastOrderDate()
        );
    }

    @Bean
    public FlatFileItemWriter<CustomerOrderSummary> csvWriter() {
        return new FlatFileItemWriterBuilder<CustomerOrderSummary>()
            .name("csvWriter")
            .resource(new FileSystemResource("customer_orders_summary.csv"))
            .delimited()
            .delimiter(",")
            .names("customerId", "totalSpend", "totalOrders", "lastOrderDate")
            .build();
    }

}
