# Handling Errors

## Goal

In this exercise you will handle the error of an invalid expiry date within the credit card charging in an automatic fashion. You will attach an error boundary event to follow another path in the payment process. Additionally you will improve the already existing test case.

## Detailed Steps

### Process Modeling
1. Open your payment process in the modeler. Attach a boundary event to the Charge credit card task. Change the event to an Error Boundary Event. Add a label to the error event like **Charging failed**.
2. Open the property panel for the error boundary event and the open the Error section. Create a Global error reference and fill a name like Charging failed, as code the value **chargingError**, as Code variable **errorCode** and as Message variable **errorMessage**.
3. For simplicity, add a Message End Event to the error event. Name the message end event Payment failed. For the implementation, select **Delegate Expression** and use the same delegate as in the other message end event: **${paymentCompletion}**.

### Updating the Charge Credit Card Delegate
4. Open the **ChargeCreditCardDelegate**.
5. Within the execute function, wrap the call to the credit card service in a try-catch-block. When you catch an **IllegalArgumentException**, throw the BPMN Error:
```java
try {
  creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
} catch (IllegalArgumentException e) {
  throw new BpmnError("chargingError", "Failed to charge credit card with card number " + cardNumber, e);
}
```
All other (technical) exceptions will automatically be thrown up to the process engine, triggering an incident visible in Cockpit.

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
assertThat(processInstance).isWaitingAt(findId("Payment requested"));
execute(job());

assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
execute(job());

assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"))
    .hasNotPassed(findId("Payment completed"))
    .hasPassed(findId("Payment failed"));
```