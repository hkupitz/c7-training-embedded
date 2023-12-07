# Create an External Task Worker With the Spring Boot Starter

## Goal

Create a new maven project and add the dependency for the `camunda-bpm-spring-boot-starter-external-task-client`.
Add implementations for an external task service.

## Detailed Steps
### Process Modeling
1. In the payment process, select the task `deduct amount from credit`
2. Change the implementation type to **External**
3. Enter a topic like `creditDeduction`

### Spring Boot application
4. Create a new maven project.
5. Enter your IDE and open the pom.xml of your project and add these dependencies:
```
<dependencies>
    <dependency>
        <groupId>org.camunda.bpm.springboot</groupId>
        <artifactId>camunda-bpm-spring-boot-starter-external-task-client</artifactId>
        <version>7.18.0</version>
    </dependency>
    <dependency>
        <groupId>javax.xml.bind</groupId>
        <artifactId>jaxb-api</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```
6. Create an new Spring Boot application class. It should implement the `main()` method. The final code looks like this:
```java
@SpringBootApplication
public class ExternalTaskWorkerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExternalTaskWorkerApplication.class, args);
  }
}
```
7. Create a configuration file. Enter the src/main/resources directory and create a file named application.yml.
8. Add the content for the basic configuration
```yaml
camunda.bpm.client:
  base-url: http://localhost:8080/engine-rest
  max-tasks: 1
  lock-duration: 20000
  worker-id: spring-boot-worker-1
logging:
  level:
    "[org.apache.http]": INFO
```

### External Task worker
9. Add a bean for the external task worker for the `creditDeduction` topic. For now, we log the invocation and simply set openAmount to 0:
```java
@SpringBootApplication
public class ExternalTaskWorkerApplication {

  Logger logger = LoggerFactory.getLogger(ExternalTaskWorkerApplication.class);
  
  public static void main(String[] args) {
    SpringApplication.run(ExternalTaskWorkerApplication.class, args);
  }

  @Bean
  @ExternalTaskSubscription("creditDeduction")
  public ExternalTaskHandler getDeductCustomerCreditWorker() {
    return (ExternalTaskHandler) (externalTask, externalTaskService) -> {
      logger.info("We received external task " + externalTask.getId());
      Map<String, Object> variables = Map.of("openAmount", 0);
      externalTaskService.complete(externalTask, variables);
    };
  }
}
```
10. Start your process application.
11. Start your worker
12. Start a new process instance.
13. Enter the IDE and check the console output of your external task worker. You should find the log statement from your handler: We received external task yyyyyyyy.

### Junit tests

14. Run your Junit tests. Many of them fail. This is because the external task is a natural wait state. We can fix our tests by adding snippets right before the point where the test fails:
    
This snippet serves if the process should charge the credit card:
```java
// execute the external task, there is an open amount
assertThat(processInstance).isWaitingAt("Activity_Deduct_Amount");
complete(externalTask(),withVariables("openAmount", 10D));
```
This snippet serves if the process should NOT charge the credit card:
```java
// execute the external task, there is an open amount
assertThat(processInstance).isWaitingAt("Activity_Deduct_Amount");
complete(externalTask(),withVariables("openAmount", 0D));
```