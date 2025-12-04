# Model a Payment Process in the Camunda Modeler

## Goal

The goal of this lab is to deploy the provided payment process to the engine. Start a process isntance from the Modeler and inspect the result in Cockpit.

## Detailed steps

1. Open the provided BPMN diagram `payment.bpmn` from the `src/main/resources` folder in the Camunda Desktop Modeler.
5. Add the technical attributes in the properties panel:
  1. Click on the canvas to access the properties of the process.
  2. Expand the General section of the properties panel and verify that the ID of the process is `PaymentProcess` and its name is `Payment Process`.
  3. Select the sequence flow that connects the XOR gateway to the "Charge credit card" task. Open the "Condition" section in the properties panel and select Expression as Type. Enter `${orderTotal > customerCredit}` as the expression.
  4. Repeat the last step for the other sequence flow leaving the XOR gateway and set the Expression to `${orderTotal <= customerCredit}`.
  5. If not done yet, run Camunda 7 via the `ProcessApplication.java` class of your Maven project.
  6. Now deploy the process model from the Modeler. Press the "Deployment" button (🚀) and enter [http://localhost:8080/engine-rest](http://localhost:8080/engine-rest) as the REST endpoint, if not present already.
6. Start a process instance from the Modeler. Press the "Start process Instance" button (▶️) and provide the necessary variables payload. You can leave the "Business Key" empty for now.
```
{
  "orderTotal": { "value": 45.99 },
  "customerCredit": { "value": 30.00 }
}
```
7. Open Cockpit and select the "Processes" tab. Select the History view of the Payment process. Inspect your process instance.
8. Start another process instance that follows the "happy path" where the customer's credit is sufficient.