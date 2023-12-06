# Handle an exception

## Goal

In this lab, you will throw an exception and then refactor the process to handle this as an incident. You will also test the behaviour

## Short description

* Mock an error in the service call towards the credit card service, throw an exception
* adjust the process model to handle the charge credit card in a new transaction
* create another test to explicitly test the error behaviour
* adjust the other test so that they still work

## Detailed steps

1. Open the *CreditCardService* class.
2. Add a new method to the *CreditCardService* to validate the expiry date:
    ```java
	boolean validateExpiryDate(String expiryDate) {
		if (expiryDate.length() != 5) {
		  return false;
		} 
		try {
		  int month = Integer.valueOf(expiryDate.substring(0, 2));
		  int year = Integer.valueOf(expiryDate.substring(3, 5)) + 2000;
		  LocalDate now = LocalDate.now();
		  if (month < 1 || month > 12 || year < now.getYear()) {
			return false;
		  }
		  if (year > now.getYear() || 
			  (year == now.getYear() && month >= now.getMonthValue())) {
			return true;
		  } else {
			return false;
		  }
		} catch (NumberFormatException|IndexOutOfBoundsException e) {
		  return false;
		}
	  }
    ```
3. Add some example code to throw an exception if an expired credit card should be charged. Add this snippet to the execute method between the LOG statements.
```java
if (validateExpiryDate(expiryDate) == false) {
  System.out.println("expiry date " + expiryDate + " is invalid");
  throw new IllegalArgumentException("invalid expiry date");
}
```
4. In the process model, select the service task **charge credit card** and tick `Asynchronous continuations > Before`.
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
  variables.put("CVC", "789");
  variables.put("expiryDate", "09/24x");
  
  // Start process with Java API and variables
  ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);
  
  // try to execute credit card payment
  assertThat(processInstance).isWaitingAt("Activity_Charge_Credit_Card");
  RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> execute(job()));
  assertThat(exception).hasMessage("Expiry date invalid!");
}
```
4. Run only this test. This should work.
5. Run all tests. Some of them fail. Why?
6. Correct the not-running tests by adding a manual job execution to pass the **Charge credit card** activity.
```java
   // assert that the process is waiting at charge credit card
   assertThat(processInstance).isWaitingAt("Activity_Charge_Credit_Card");
   execute(job());
```
7. Now, all tests are ok again.