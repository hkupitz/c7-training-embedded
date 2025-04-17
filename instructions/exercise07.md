# Handle an exception

## Goal

In this lab, you will throw an exception and then refactor the process to handle this as an incident. You will also test the behavior.

## Detailed steps

1. Open the *CreditCardService* class.
2. Inspect the `validateExpiryDate()` method. It checks if the supplied expiry date has the right length and is valid with regards to the current date.
3. Use it to throw an exception if a credit card with an invalid expiry date is supposed to be charged. Add this snippet to the `chargeAmount()` method between the LOG statements.
    ```java
    if (validateExpiryDate(expiryDate) == false) {
      LOG.info("expiry date " + expiryDate + " is invalid");
      throw new IllegalArgumentException("Expiry date invalid!");
    }
    ```
4. In the process model, select the service task **Charge credit card** and tick `Asynchronous continuations > Before`.
5. Insert another test in the unit test class:
    ```java
    @Test
    @Deployment(resources = "payment_process.bpmn")
    public void testInvalidExpiryDate(){
      Mocks.register("paymentCompletion", (JavaDelegate) execution -> {});
      
      // Create a HashMap to put in variables for the process instance
      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("orderTotal", 30.00);
      variables.put("customerId", "cust20");
      variables.put("cardNumber", "1234 5678");
      variables.put("cvc", "789");
      variables.put("expiryDate", "09/26x");
      
      // Start process with Java API and variables
      ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);

      execute(job());
    
      // try to execute credit card payment
      assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
      RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> execute(job()));
      assertEquals(exception.getMessage(), "Expiry date invalid!");    
    }
    ```
4. Run only this test. This should work.
5. Run all tests. Some of them fail. Why?
6. Correct the non-running tests by adding a manual job execution to pass the **Charge credit card** activity.
    ```java
    assertThat(processInstance).isWaitingAt("Activity_Charge_Credit_Card");
    execute(job());
    ```
7. Now, all tests should be passing again.