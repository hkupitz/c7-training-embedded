# Handling Errors

## Goal

In this exercise you will handle an error that happened in the credit card service. You will use an attached error boundary event to follow another path in the payment process.

## Detailed Steps

### Process Modeling
1. Open your payment process in the modeler. Attach a boundary event to the Charge credit card task. Transform the event to an Error Boundary Event. Add a label to the error event like **Invalid expiry date**.
2. Open the property panel for the error boundary event and the open the Error section. Create a Global error reference and fill a name like Charging failed, as code the value **chargingError**, as Code variable **errorCode** and as Message variable **errorMessage**.
3. For simplicity, add a Message End Event to the error event. Name the message end event Payment failed. For the implementation, select delegate expression and use the same delegate as in the other message end event: **${paymentCompletion}**.

### Updating the Charge Credit Card Delegate
4. Open the **ChargeCreditCardDelegate**.
5. Within the execute function, wrap the call to the credit card service in a try-catch-block. When you catch an exception, throw the BPMN Error:
```java
try {
  creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
} catch (IllegalArgumentException e) {
  throw new BpmnError("chargingError", "Failed to charge credit card with card number " + cardNumber, e);
}
```

### JUnit Testing

6. Adjust your `testInvalidExpiryDate()` method to verify that the error got caught:
```java
assertThat(processInstance).isWaitingAt(findId("Payment requested"));
execute(job());

assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
execute(job());

assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"))
    .hasNotPassed("ID of Payment completed End Event")
    .hasPassed(findId("Payment failed"));
```