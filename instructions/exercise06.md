# Test the process end-to-end

## Goal

In this lab, we will write an end-to-end test for the order and the payment processes.

## Detailed steps

1. Create a new test method `testEndToEnd`. Do not override the currently used mocks.
    ```java
    @Test
    public void testEndToEnd(){
      ProcessInstance orderProcessInstance = runtimeService().startProcessInstanceByKey(
        "OrderProcess", "Test 1",
        withVariables("orderTotal", 30.00,
          "customerId", "cust30",
          "cardNumber", "1234 5678",
          "cvc", "123",
          "expiryDate", "09/26"
        )
      );

      assertThat(orderProcessInstance).isEnded();
    }
    ```
3. Run all tests again. The end-to-end test should fail.
4. Rename the ID of the intermediate message catch event of your order process to "Order_Event_PaymentCompleted".
5. For the end-to-end test we currently only have the process instance of the order process available. We need to query the process instance of the payment process before to do the assertion and execute the job:
   ```java
   assertThat(orderProcessInstance).isWaitingAt("Order_Event_PaymentCompleted");

   ProcessInstance paymentProcessInstance = processInstanceQuery().processDefinitionKey("PaymentProcess").singleResult();

   assertThat(paymentProcessInstance).isWaitingAt(findId("Payment requested"));
   execute(job());

   assertThat(paymentProcessInstance).isEnded();
   assertThat(orderProcessInstance).isEnded();
   ```
6. Run the test again. It should pass now.