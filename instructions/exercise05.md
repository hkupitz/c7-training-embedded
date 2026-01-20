# Message Events

## Goal

You create a new process model to handle orders. The payment process gets invoked by receiving a message from the order process. The order process continues once the payment is completed.

## Detailed steps

### Process modeling

1. Open the Modeler and create a new BPMN diagram. Model an order process with the following elements:
    * Start event **Order received**
    * Send task **Invoke payment**
    * Receive event **Payment completed**
    * End event **Order completed**

2. Fill in the technical attributes:
    1. Process ID: **OrderProcess**
    2. Process Name: **Order Process**
    3. History cleanup: Time to live 30
    4. Send Task implementation: Type - **Delegate expression**, Delegate expression - **${invokePayment}**
    5. Message Intermediate Catch Event: Open the Message section in the properties panel and add a new "Global message reference". Enter **paymentCompletedMessage** as the Name.
3. Save the process model in the `src/main/resources` folder of your project. Name it **order.bpmn**.

### Message sending

4. Create a new delegate: `InvokePaymentDelegate`.
    ```java
    package io.camunda.training.delegates;

    import org.camunda.bpm.engine.delegate.DelegateExecution;
    import org.camunda.bpm.engine.delegate.JavaDelegate;
    import org.camunda.bpm.engine.runtime.ProcessInstance;
    import org.springframework.stereotype.Component;

    import java.util.UUID;

    @Component("invokePayment")
    public class InvokePaymentDelegate implements JavaDelegate {

      @Override
      public void execute(DelegateExecution execution) throws Exception {
        
        // Delegate implementation

      }
    }
    ```

5. To send the **paymentRequestMessage** to the payment process and to pass all variables and the business key from the order process to the payment process, add the following implementation in the `execute` method. Create a unique business key (order ID) that gets passed on to the payment process as well.
    ```java
      // Generate unique business key
      String orderId = UUID.randomUUID().toString();

      // Invoke payment process via message and pass on all variables
      ProcessInstance processInstance = execution
              .getProcessEngineServices()
              .getRuntimeService()
              .createMessageCorrelation("paymentRequestMessage")
              .setVariables(execution.getVariables())
              .processInstanceBusinessKey(orderId)
              .correlateStartMessage();

      // Store business key for order process as well
      execution.setProcessBusinessKey(orderId);
   ```

### Message receiving

6. Open the Modeler and open the payment process. Change the start event to a Message Start Event. Open the Message section in the property panel and add a new "Global message reference". Enter **paymentRequestMessage** as Name.
7. Change the end event to a Message End Event. Fill the Implementation with type **Delegate expression** and the expression `${completePayment}`.
8. Create another delegate that sends a message back to the order process. In this implementation the correlation happens via the previously generated business key.
    ```java
    package io.camunda.training.delegates;

    import org.camunda.bpm.engine.delegate.DelegateExecution;
    import org.camunda.bpm.engine.delegate.JavaDelegate;
    import org.springframework.stereotype.Component;

    @Component("completePayment")
    public class CompletePaymentDelegate implements JavaDelegate {

      @Override
      public void execute(DelegateExecution execution) throws Exception {

        // Send a message back to the order process to continue its execution
        execution.getProcessEngineServices()
                .getRuntimeService()
                .createMessageCorrelation("paymentCompletionMessage")
                .processInstanceBusinessKey(execution.getBusinessKey())
                .correlate();
      }
    }
    ```

### Acceptance testing

11. Start a process instance from the modeler using the following payload:

    ```json
    {
      "customerId": { "value": "cust20" },
      "orderTotal": { "value": 40, "type": "Double" },
      "cardNumber": { "value": "1234 5678" },
      "cvc": { "value": "123" },
      "expiryDate": { "value": "08/26" }
    }
    ```
    What happens?

12. Enable "Asynchronous continuations: After" for the message start event in the payment process. Now both process instances should get started sequentially and run through correctly.

⚠️ Without an asynchronous continuation, the order process instance has not been persisted in the Camunda database yet. Message correlation does not work in-memory, but needs a persisted process instance state.

### Unit testing

8. Adjust the `@Deployment` annotation: `@Deployment(resources = {"payment.bpmn", "order.bpmn"})`
9. Create a new test method `testOrderProcess`. Don't forget the `@Test` annotation:
    ```java
    @Test
    public void testOrderProcess() {

      // Not starting the payment process for this test
      Mocks.register("invokePayment", (JavaDelegate) execution -> {});

      ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("OrderProcess", "Test 1");

      runtimeService().correlateMessage("paymentCompletionMessage");
      assertThat(processInstance).isEnded();
    }
    ```
10. Extend the `setup()` method in your unit test class to register all mocks:
    ```java  
    Mocks.register("invokePayment", new InvokePaymentDelegate());
    Mocks.register("completePayment", new CompletePaymentDelegate());
    ```

    At the same time, add the following line at the top of both `testCreditCardPath` and `testCreditSufficientPath` to not execute the respective delegate in the payment process tests:
    ```java  
    Mocks.register("completePayment", (JavaDelegate) ex -> {});
    ```
11. Add the following snippet after starting the process instance in your `testCreditCardPath()` method to trigger the job (async after) programmatically as the job executor is disabled during unit tests:
    ```java
    assertThat(processInstance).isStarted();
    execute(job());
    ```
12. All three tests should pass and the coverage for both process models should be 100%.