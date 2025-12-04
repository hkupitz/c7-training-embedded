# Handle an exception

## Goal

In this lab, you will throw an exception that will be shown as an incident. Additionally you will also test the behavior.

## Detailed steps

1. Open the *CreditCardService* class.
2. Inspect the `validateExpiryDate()` method. It checks if the supplied expiry date has the right length and is valid with regards to the current date.
3. Use it to throw an exception if a credit card with an invalid expiry date is supposed to be charged. Add this snippet to the `chargeAmount()` method after the first LOG statement.
    ```java
    if (!validateExpiryDate(expiryDate)) {
      LOG.info("expiry date " + expiryDate + " is invalid");

      throw new IllegalArgumentException("Expiry date invalid!");
    } else {
      LOG.info("payment completed");
    }
    ```
4. In the process model, select the service task **Charge credit card** and tick `Asynchronous continuations > Before`. This makes sure the incident does a rollback to before the service task.
5. Insert another test in the unit test class:
    ```java
    @Test
    public void testInvalidExpiryDate(){
      Mocks.register("paymentCompletion", (JavaDelegate) execution -> {});

      Map<String, Object> variables = new HashMap<String, Object>();
      variables.put("orderTotal", 30.00);
      variables.put("customerId", "cust20");
      variables.put("cardNumber", "1234 5678");
      variables.put("cvc", "789");
      variables.put("expiryDate", "09/26x");

      ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);

      // Execute start event job
      assertThat(processInstance).isStarted();
      execute(job());

      // Simulate credit card error
      assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
      RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> execute(job()));
      assertEquals("Expiry date invalid!", exception.getMessage()); 
    }
    ```
4. Run only this test. This should work.
5. Run all tests. The credit card path test fails.
6. Correct the test method by adding a manual job execution to pass the **Charge credit card** activity.
    ```java
    assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
    execute(job());
    ```
7. Now, all tests should be passing again.