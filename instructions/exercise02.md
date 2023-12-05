# Create a JUnit test

## Goal
The goal of this lab is to build your first JUnit test case for a BPMN 2.0 process with the help of the `camunda-bpm-assert` library. You will create a JUnit test to see if the process behaves as expected with the data used in the test. You add configurations to the test engine and its logging output.

## Short description

* Move the bpmn file to the correct position inside your project
* Add the required dependencies to `camunda-bpm-junit5`, `camunda-bpm-assert`, and `assertj-core`
* Prepare your IDE to handle static imports of `camunda-bpm-assert`
* Create a Junit test and start a process instance with variables for a payment that needs customer credit and a credit card. Assert that the process instance is ended right after the start
* Add a configuration file for the process engine picked up by the Junit extension to use an in-memory database
* Run the Junit test and see it succeed. Check the output in the console

## Detailed steps

1. Move your bpmn file to `src/main/resources/payment.bpmn`. It will then be on the classpath of your packaged process application and can be used for automated deployment and testing.
2. Add the required dependencies to the pom.xml:
   ```xml
    <dependency>
      <groupId>org.camunda.bpm.extension</groupId>
      <artifactId>camunda-bpm-junit5</artifactId>
      <version>1.0.2</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.camunda.bpm.assert</groupId>
      <artifactId>camunda-bpm-assert</artifactId>
      <version>12.0.0</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.assertj</groupId>
      <artifactId>assertj-core</artifactId>
      <version>3.19.0</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.camunda.bpm.extension</groupId>
      <artifactId>camunda-bpm-process-test-coverage-junit5</artifactId>
      <version>1.0.3</version>
      <scope>test</scope>
    </dependency>    
   ```
3. Open the JUnit test class `ProcessTests.java` from the folder `src/test/java` and inspect the content.
4. Prepare your IDE to handle the static imports of `camunda-bpm-assert` and `assertJ`. In Eclipse go to **Window > Preferences > Java > Editor > Content Assist > Favorites > New Type...** and add the following types: `org.camunda.bpm.engine.test.assertions.ProcessEngineTests` and `org.assertj.core.api.Assertions`. Also, go to **Window > Preferences > Java > Code Style > Organize Imports** and set "Number of static imports needed for .\*" to "0". IntelliJ should pick up the **pom.xml** updates and prompt you to import the changes.
5. Add the static imports for the Assertions and camunda-bpm-assert support into the import section of the class:
   ```java
   import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
   import static org.assertj.core.api.Assertions.*;
   ```
6. Add the process to test with the
   ```java
   @Deployment(resources = "your bpmn file.bpmn")
   ```
   annotation. The annotation has to be added to the test class. Specify the correct file name for your BPMN model.
7. Add the ProcessEngineExtension as a JUnit 5 extension to the test class.
   ```java
   @ExtendWith(ProcessEngineCoverageExtension.class)
   ```
8. At the start of the test code, we create a Map of type String, Object to use to put in our variables. Then we use the runtimeService() API to start a process instance using the ID (aka ‘key’ as you’ll see in the API call below) of the process template. We also provide the variables HashMap as an argument to the method. Finally, we utilize our assertion library to make sure that our process has indeed completed.
   ```java
   // Create a HashMap to put in variables for the process instance
   Map<String, Object> variables = new HashMap<>();
   variables.put("orderTotal", 30.00);
   variables.put("customerCredit", 20.00);
   
   // Start process with Java API and variables
   ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);
   
   // Make assertions on the process instance
   assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"));
   ```
9. The process engine used in the rule in the JUnit test above needs to be configured. To do this, open the file named camunda.cfg.xml under `src/test/resources` and fill it with the content below. This configuration uses an in memory database, emits a full audit (history) trail, uses a configurable expression manager (mocks), and has a placeholder for further extensions (plugins).
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
      <bean id="processEngineConfiguration" class="org.camunda.bpm.extension.process_test_coverage.engine.ProcessCoverageInMemProcessEngineConfiguration">
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
10. As spring boot defaults the logging level in tests to DEBUG (which is verbose), you can create a logging configuration at `src/test/resources` with the name `logback-test.xml`. In this example we configure some levels to be more silent and concentrate on the output of the camunda engine:
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
11. Run the Junit test. Everything should work.