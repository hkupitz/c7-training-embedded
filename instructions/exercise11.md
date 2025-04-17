# Create an External Task Worker With the Spring Boot Starter

## Goal

Create a new Maven project to implement an external task worker that replaces the "Refund credit" Java Delegate.

## Detailed Steps
### Process Modeling
1. In the payment process, select the task `deduct amount from credit`
2. Change the implementation type to **External**
3. Enter a topic like `credit-refund`

### Spring Boot application
4. Create a new Maven project.
5. Open the pom.xml of your project and add the following dependencies:
    ```
    <dependencies>
        <dependency>
            <groupId>org.camunda.bpm.springboot</groupId>
            <artifactId>camunda-bpm-spring-boot-starter-external-task-client</artifactId>
        </dependency>

        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-impl</artifactId>
            <version>4.0.5</version>
        </dependency>
    </dependencies>
    ```
6. Create a new Spring Boot application class. It should implement the `main()` method. The final code looks like this:
```java
@SpringBootApplication
public class ExternalTaskWorkerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExternalTaskWorkerApplication.class, args);
  }
}
```
7. Create a configuration file. Enter the `src/main/resources` directory and create a file named `application.yml`.
8. Add the content for the basic configuration
```yaml
camunda.bpm.client:
  base-url: http://localhost:8080/engine-rest
  max-tasks: 1
  lock-duration: 20000
  worker-id: spring-boot-worker-1

server.port: 8081

logging.level.org.camunda.bpm.client: INFO
```

### External Task worker
9. Add a bean for the external task worker for the `creditDeduction` topic. For now, we log the invocation and simply set openAmount to 0:
```java
@Component
@ExternalTaskSubscription("credit-refund")
public class RefundCreditWorker implements ExternalTaskHandler {

    @Override
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        System.out.println("Refunding credit");
    }
}
```
10. Start your process application.
11. Start your worker
12. Start a new process instance.
13. Open the IDE and check the console output of your external task worker. You should find the corresponding log statement.