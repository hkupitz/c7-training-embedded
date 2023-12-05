# Model a Payment Process in the Camunda Modeler

## Goal

The goal of this lab is to create a process model in Camunda Modeler and deploy it to the engine. Start a process isntance form the Modeler and inspect the result in the Cockpit.

## Detailed steps

1. Download the Camunda Modeler from the [official download page.](http://camunda.org/download/modeler/)
2. Unpack the downloaded archive and start the Modeler.
3. Model the payment process.
  * The customer can have an existing credit at the company
  * The amount should be deducted from this credit
  * If the customer has to pay more than his credit, then the credit card of the customer should be charged.
4. Save your work, which will save the BPMN 2.0 XML to your disk as well. If you want, take a look at it using the XML tab.
5. Add the technical attributes in the property panel:
  1. Click on the canvas to access the properties of the process.
  2. Expand the general section of the property panel and change the ID of the process to `PaymentProcess` and enter `Payment process` for the Name.
  3. Select the sequence flow from the XOR gateway to the Charge credit card task. enter the section for the Condition and select Expression as Type. The expression is `#{orderTotal > customerCredit}`.
  4. Repeat the last step for the other sequence flow leaving the XOR gateway and set the Expression to `#{orderTotal <= customerCredit}`.
  5. Start Camunda 7 from your IDE. 
  6. Now deploy the process model from the Modeler to Camunda 7. Press the Deployment button  (rocket) and fill [http://localhost:8080/engine-rest](http://localhost:8080/engine-rest) as REST Endpoint.
6. Start a process instance from the Modeler. Press the Start process Instance button (play) and provide variables:
```
{
  "orderTotal": {"value": 45.99},
  "customerCredit": {"value": 30.00}
}
```
7. Enter Cockpit and select the Process dashboard. Select the History view of the Payment process. Inspect your process instance.

## Summary

In this exercise you have modeled an executable process. After deploying it to the engine, you started a process instance from the Modeler. You have seen the history in Cockpit.