# Message Events

## Goal

You create a new process model to handle orders. The payment gets started by receiving a message from the order process. The order process continues once the payment is completed.

## Detailed steps

### Process modeling

1. Enter the Modeler and create a new BPMN diagram. Model an order process with the following elements:
    * Start event **Order received**
    * Send task **Invoke payment**
    * Receive event **Payment completed**
    * End event **Order completed**

2. Fill in the technical attributes:
    1. Process Id: **OrderProcess**
    2. Process name: **Order process**
    3. History Cleanup: Time to live 30
    4. Send Task implementation: Type **Delegate Expression**, Delegate expression **${paymentRequest}**
    5. Message Intermediate Catch Event: Open the Message section in the property panel and add a new Global message reference. Enter **paymentCompletedMessage** as Name.
3. Save the process model in the src/main/resources folder of your project. Name it **order_process.bpmn**.

### Message sending

4. Create a new class `SendPaymentRequestDelegate`. It should implement the `JavaDelegate` interface.
    ```java
   package org.camunda.training;

   import org.camunda.bpm.engine.delegate.DelegateExecution;
   import org.camunda.bpm.engine.delegate.JavaDelegate;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.stereotype.Component;
   
   @Named("paymentRequest")
   public class SendPaymentRequestDelegate implements JavaDelegate {
   
     private static final Logger LOG = LoggerFactory.getLogger(SendPaymentRequestDelegate.class);
   
     @Override
     public void execute(DelegateExecution execution) throws Exception {
     
     }
   }
    ```

5. To send the **paymentRequestMessage** to the payment process and to pass all variables and the business key from the order process to the payment process, add the following implementation in the `execute` method. The result of correlation returns the process instance id of the payment process. It will be saved as a new process variable when the send task gets completed.
    ```java
    ProcessInstance processInstance = execution
        .getProcessEngineServices()
        .getRuntimeService()
        .createMessageCorrelation("paymentRequestMessage")
        .setVariables(execution.getVariables())
        .processInstanceBusinessKey(execution.getProcessBusinessKey())
        .correlateStartMessage();
    execution.setVariable("paymentProcessInstanceId", processInstance.getId());
    ```

### Message receiving

6. Enter the Modeler and open the payment process. Change the start event to a Message Start Event. Open the Message section in the property panel and add a new Global message reference. Enter **paymentRequestMessage** as Name.
7. Change the end event to a Message End Event. Fill the Implementation with type `DelegateExpression` and Delegate expression `${paymentCompletion}`.
8. Create another delegate that sends a message back to the origin process. The correlation happens via businessKey in this implementation.
   ```java
   package org.camunda.training;

   import org.camunda.bpm.engine.delegate.DelegateExecution;
   import org.camunda.bpm.engine.delegate.JavaDelegate;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   
   import javax.inject.Named;
   
   @Named("paymentCompletion")
   public class SendPaymentCompletionDelegate implements JavaDelegate {
   
     private static final Logger LOG = LoggerFactory.getLogger(SendPaymentRequestDelegate.class);
   
     @Override
     public void execute(DelegateExecution execution) throws Exception {
       execution
       .getProcessEngineServices()
       .getRuntimeService()
       .createMessageCorrelation("paymentCompletedMessage")
       .processInstanceBusinessKey(execution.getBusinessKey())
       .correlate();
     }
   }
   ```

### JUnit testing

9. Create a new test method `testOrderProcess`. Don't forget the annotations `@Test` and `@Deployment`:
   ```java
   @Test
   @Deployment(resources = "order_process.bpmn")
   public void testOrderProcess() {
     // I will not start the other process now
     Mocks.register("paymentRequest", (JavaDelegate) execution -> {});
     ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("OrderProcess", "Test 1", withVariables(
     "orderTotal",        30.00,
     "customerId",        "cust30",
     "cardNumber",        "1234 5678",
     "CVC",        "123",
     "expiryDate",        "09/24"
     ));
     runtimeService().correlateMessage("paymentCompletedMessage");
     assertThat(processInstance).isEnded();
   }
   ```
10. Extend the `setup`-method in your Junit test class to register all mocks:
   ```java
    Mocks.register("paymentRequest", new SendPaymentRequestDelegate());
    Mocks.register("paymentCompletion", new SendPaymentCompletionDelegate());
   ```
   At the same time, add a snippet above `testHappyPath` and `testCreditSufficient`:
   ```java
   Mocks.register("paymentCompletion", (JavaDelegate) ex -> {});
   ```

### Acceptance testing

11. Start a process instance from the modeler using this payload:
    ```json
    {
      "variables": {
        "orderTotal": {
          "value": 49.99,
          "type": "Double"
        },
        "customerId": {
          "value": "cust25",
          "type": "String"
        },
        "cardNumber": {
          "value": "1234 5678"
        },
        "CVC": {
          "value": "123"
        }, 
        "expiryDate": {
          "value": "09/24"
        }
      },
      "businessKey": "Order test 1"
    }    
    ```
    What happens?
12. We will deal with the problem in the next exercise.

### Summary

This exercise you have added a process model to handle orders. It starts the payment process by using message correlation and waits until the payment process is finished. The payment process sends a message back to the order process.