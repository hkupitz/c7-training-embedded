# Add a Decision Table About Discount

## Goal

Add a Business Rule Task to the order process and calculate a discount to the order based on the amount.

## Detailed steps
### Decision Modeling
1. Open the Modeler and create a new DMN diagram.
2. Name the decision table **Order discount** and set the Id of the decision table to **orderDiscount**.
3. Enter the decision table and name the input column to **Order amount**. Set the input expression to **orderTotal**. Select **double** as type to match your amount variable type.
4. Label the output column to Discount percentage and name the output as **discount**. Set the type to **integer**.
5. Add some rules to discount some orders. An example which include less than, greater or equal to, along with ranges is shown below. Use Unique for the Hit Policy. Unique Hit Policy should only satisfy one row.
![image](https://user-images.githubusercontent.com/5269168/195629261-549a3e16-dc5e-4555-b444-5177ad432a30.png)
7. Save the decision table to your src/main/resources folder.
8. Simulate your decision rule with the [DMN simulator](https://consulting.camunda.com/dmn-simulator/). Drag the dmn file on the canvas of the simulator. Enter different values. You can edit the decision table in the siumlator as well and download the changed file to your computer.

### Process Modeling
9. Open the order process in the Modeler.
10. Add a task to the order process to get the discount for the order amount before the payment invocation. Name the task **Get discount**. Change the task type to Business Rule Task.
11. In the Implementation section of the property panel select DMN as Type. Add the Id of the decision table **orderDiscount** as Decision reference. Fill the Result variable with discount. Select **singleEntry** as value for Map decision result.
12. Add a task between the Get discount and Invoke payment tasks to apply the discount to the order amount. Name the task **Apply discount**.
13. Change the task type to Script Task. Open the Script section. Enter **javascript** as Format (the script language). Select Inline script as Type. Enter the Script
```javascript
orderTotal - (orderTotal * discount / 100)
```
14. Name the Result variable **discountedAmount**.
15. To pay only the discounted amount, map the order total that is passed to the payment process with an Input mapping on the **Invoke payment** task. In the **paymentRequest** implementation, the local variable is preferred over the process variable. Select the Invoke payment task.
16. Enter the Input section. Click on the + to add an input mapping. Enter **orderTotal** as the Local variable name. Select String or Expression as the Assignment type. Enter **${discountedAmount}** as the Value with the expression to access the result from the decision evaluation.

### Acceptance Test
17. Start a process instance.
18. Enter Cockpit and check the history of the order and the payment process. Is the discount applied correctly?

### Junit tests

19. Run your Junit tests. They are failing as they are missing the deployment of the dmn file. Add the file to the deployed resources.

### Summary
In this exercise you have modeled a DMN decision table to get a discount for a given order amount. You connected the decision table with a Business Rules Task to the order process. A new Script Task applied the discount to the order total. The discounted amount was mapped as with an input mapping as the orderTotal amount to the payment process.
