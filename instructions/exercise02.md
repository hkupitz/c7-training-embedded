# Create a JUnit test

## Goal
The goal of this lab is to build your first JUnit test case for a BPMN 2.0 process with the help of the `camunda-bpm-assert` library. You will create a unit test to verify that the process behaves as expected.

## Detailed steps

1. The BPMN file, in this case `payment.bpmn` already is on the classpath of your packaged process application and can be used for automated deployment and testing.
2. Add the required dependencies to the pom.xml if they are not present yet:
   ```xml
    <dependency>
      <groupId>org.camunda.bpm</groupId>
      <artifactId>camunda-bpm-junit5</artifactId>
      <version>${camunda.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.camunda.bpm</groupId>
      <artifactId>camunda-bpm-assert</artifactId>
      <version>${camunda.version}</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.camunda.community.process_test_coverage</groupId>
      <artifactId>camunda-process-test-coverage-junit5-platform-7</artifactId>
      <version>2.8.1</version>
      <scope>test</scope>
    </dependency>
   ```
3. Open the JUnit test class `ProcessTests.java` from the folder `src/test/java` and inspect its content.
4. Prepare your IDE to handle the static imports of `camunda-bpm-assert` and `assertJ`. In Eclipse go to **Window > Preferences > Java > Editor > Content Assist > Favorites > New Type...** and add the following types: `org.camunda.bpm.engine.test.assertions.ProcessEngineTests` and `org.assertj.core.api.Assertions`. Also, go to **Window > Preferences > Java > Code Style > Organize Imports** and set "Number of static imports needed for .\*" to "0".
5. Add the static imports for the assertions and `camunda-bpm-assert` library in the import section:
   ```java
   import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
   import static org.assertj.core.api.Assertions.*;
   ```
6. Add the following annotation to automatically deploy the BPMN process when starting the tests.
   ```java
   @Deployment(resources = "payment.bpmn")
   ```
7. Add the `ProcessEngineCoverageExtension` as a JUnit 5 extension to the test class.
   ```java
   @ExtendWith(ProcessEngineCoverageExtension.class)
   ```
8. At the start of the test code, create a `Map` of the type `<String, Object>` to define the process instance payload. Then use the `runtimeService()` of the Java API to start a process instance using the ID (aka "key") of the payment process. You can then pass the variables payload to the `startProcessInstanceByKey` method. Finally, utilize the assertion library to make sure that the process instance correctly ran through and completed.
   ```java
   // Create a HashMap for the variables payload
   Map<String, Object> variables = new HashMap<>();
   variables.put("orderTotal", 30.00);
   variables.put("customerCredit", 20.00);
   
   // Start process via Java API
   ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);
   
   // Make assertions on the process instance
   assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"));
   ```
9. The process engine used in the JUnit test class now needs to be configured. To do this, open the file named `camunda.cfg.xml` under `src/test/resources` and fill it with the content below. The used configuration provides an in memory process engine & database, emits a full process (history) trail, uses a configurable expression manager (for mocking), and has a placeholder for further extensions (plugins).
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
      <bean id="processEngineConfiguration" class="org.camunda.community.process_test_coverage.engine.platform7.ProcessCoverageInMemProcessEngineConfiguration">
        <property name="history" value="full" />
        <property name="expressionManager">
          <bean class="org.camunda.bpm.engine.test.mock.MockExpressionManager"/>
        </property>
        <property name="processEnginePlugins">
          <list></list>
        </property>
      </bean>
    </beans>
    ```
10. As Spring Boot defaults the logging level in tests to DEBUG (which is verbose), you can create a logging configuration under `src/test/resources` with the name `logback-test.xml`. In this example we configure the most important loggers to be more silent, so the focus lies on the engine output.
   ```xml
    <configuration>
      <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
          <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
      </appender>
   
      <root level="debug">
        <appender-ref ref="STDOUT" />
      </root>
   
      <logger name="org.apache.ibatis" level="info" />
      <logger name="javax.activation" level="info" />
      <logger name="org.springframework" level="info" />
   
      <logger name="org.camunda" level="info" />
      <logger name="org.camunda.bpm.engine.test" level="debug" />
    </configuration>
   ```
11. Run the test class and verify that the existing test passes.
12. Check the generated test coverage report. We will complete the test coverage in a later exercise.