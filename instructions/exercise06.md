# Test the process end-to-end

## Goal

In this lab, we will write an end-to-end test for the order process and the payment process.

## Detailed steps

1. Create a new test method `testEndToEnd`. Do not override the currently used mocks.
    ```java
    @Test
    @Deployment(resources = {"order.bpmn", "payment.bpmn"})
    public void testEndToEnd(){
      ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
        "OrderProcess",
        "Test 1",
        withVariables("orderTotal", 30.00,
          "customerId", "cust30",
          "cardNumber", "1234 5678",
          "cvc", "123",
          "expiryDate", "09/26"
        )
      );
      assertThat(processInstance).isEnded();
    }
    ```
2. In the payment process, select the start event and tick `Asynchronous continuations > After`.
3. Run all tests again. Some are failing. Why?
4. For the failing tests, you will need to execute the job from the start event of the payment process. To do this, insert this at the right point:
   ```java
   assertThat(paymentProcess).isWaitingAt(findId("Payment requested"));
   execute(job());
   ```
5. For the end-to-end test, this will not work as we only have the process instance of the order process available. We need to query the process instance of the payment process before to do the assertion and execute the job:
   ```java
   assertThat(processInstance).isWaitingAt(findId("Payment requested"));
   ProcessInstance paymentProcess = processInstanceQuery().processDefinitionKey("PaymentProcess").singleResult();
   assertThat(paymentProcess).isWaitingAt("StartEvent_Payment_Required");
   execute(job());
   assertThat(paymentProcess).isEnded();
   ```
6. Now, all tests should work again.