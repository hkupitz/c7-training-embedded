# Create an External Task Worker With the Spring Boot Starter

## Goal

Create a new Maven project to implement an external task worker that replaces the "Refund credit" Java Delegate.

## Detailed Steps
### Process modeling
1. In the payment process, select the service task **Refund credit**.
2. Change the implementation type to **External**
3. Enter the topic **credit-refund**.

### Spring Boot application
4. Create a new Maven project.
5. Open the **pom.xml** of your project and add the following dependencies:
    ```
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>3.5.7</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.camunda.bpm.springboot</groupId>
            <artifactId>camunda-bpm-spring-boot-starter-external-task-client</artifactId>
            <version>7.24.0</version>
        </dependency>

        <dependency>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-impl</artifactId>
            <version>4.0.5</version>
        </dependency>
    </dependencies>
    ```
6. Create a new Spring Boot application class. It should implement the `main()` method:
    ```java
    @SpringBootApplication
    public class ExternalTaskWorkerApplication {

      public static void main(String[] args) {
        SpringApplication.run(ExternalTaskWorkerApplication.class, args);
      }
    }
    ```
7. Create a configuration file `application.yml` under the `src/main/resources` path.
8. Add the following content:
    ```yaml
    camunda.bpm.client:
      base-url: http://localhost:8080/engine-rest
      max-tasks: 1
      lock-duration: 20000
      worker-id: spring-boot-worker-1

    server.port: 8081

    logging.level.org.camunda.bpm.client: INFO
    ```

### External task worker
9. Add a bean for the external task worker that subscribes to the `creditDeduction` topic. For now we just log the invocation and complete the task:
    ```java
    @Component
    @ExternalTaskSubscription("credit-refund")
    public class RefundCreditWorker implements ExternalTaskHandler {

        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            System.out.println("Refunding credit");        
            
            externalTaskService.complete(externalTask);
        }
    }
    ```
10. Start your process application.
11. Start your worker application.
12. Start a new process instance.
13. Open the IDE and check the console output of your external task worker. You should find the corresponding log statement.

### JUnit tests

24. Run your unit tests. `testInvalidExpiryDate` should fail. This is because of the newly added external service task whose completion needs to be mocked. Add this snippet before the final assertions to achieve this:
    ```java
    // Complete external compensation task
    complete(externalTask());
    ```