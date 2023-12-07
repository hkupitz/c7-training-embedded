# Handling Errors

## Goal

In this exercise you will handle an error that happened in the credit card service. You will use an attached error boundary event to follow another path in the payment process. You will improve the test of exercise 4.

## Detailed Steps

### Process Modeling
1. Open your payment process in the modeler. Attach a boundary event to the Charge credit card task. Change the event to an Error Boundary Event. Add a label to the error event like **Charging failed**.
2. Open the property panel for the error boundary event and the open the Error section. Create a Global error reference and fill a name like Charging failed, as code the value **chargingError**, as Code variable **errorCode** and as Message variable **errorMessage**.
3. For simplicity, add a Message End Event to the error event. Name the message end event Payment failed. For the implementation, select delegate expression and use the same delegate as in the other message end event: **${paymentCompletion}**.

### Updating the Charge Credit Card Delegate
4. Open the **ChargeCreditCardDelegate**.
5. Within the execute function, wrap the call to the credit card service in a try-catch-block. When you catch an exception, throw the BPMN Error:
```java
try {
  creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
} catch (Exception exc) {
  throw new BpmnError("chargingError", "We failed to charge credit card with card number " + cardNumber, exc);
}
```

### JUnit Testing

6. Add a new test method with the name testInvalidExpiryDate(). Add the @Test annotation.
```java
@Test
@Deployment(resources = "payment_process.bpmn")
public void testInvalidExpiryDate() {
}
```
7. Mock the **paymentCompletion** delegate, so that no message is sent:
```java
Mocks.register("paymentCompletion", (JavaDelegate)execution -> {});
```
8. Start the payment process. Make sure to use an invalid expiry date:
```java
Map<String, Object> variables = new HashMap<String, Object>();
variables.put("orderTotal", 30.00);
variables.put("customerId", "cust20");
variables.put("cardNumber", "1234 5678");
variables.put("CVC","123");
variables.put("expiryDate","09/241");
// Start process with Java API and variables
ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);
```
9. In your test, start the job of the process and add assertions to verify that the error got caught:
```java
assertThat(processInstance).isWaitingAt("StartEvent_Payment_Required");
execute(job());
assertThat(processInstance).isWaitingAt("Activity_Charge_Credit_Card");
execute(job());
// Make assertions on the process instance
assertThat(processInstance).isEnded().hasPassed("Activity_Charge_Credit_Card")
    .hasNotPassed("Event_Payment_Complete")
    .hasPassed(<payment failed end event id>);
```