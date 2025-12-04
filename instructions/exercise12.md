# Add a Decision Table About Discount

## Goal

Add a Business Rule Task to the order process and calculate a discount for the order based on the amount.

## Detailed Steps
### Decision modeling
1. Open the Modeler and create a new DMN diagram.
2. Name the decision table **Order discount** and set the ID of the decision table to **OrderDiscount**.
3. Also set a history cleanup time to live of **30**.
4. Open the decision table and name the input column **Order total**. Set the input expression to **orderTotal**. Select **double** as type to match your amount variable type.
5. Label the output column **Discount percentage** and set the output name to **discount**. Choose **integer** as the type.
6. Add some rules to discount orders. An example is shown below. Use **Unique** as the hit policy.
![image](https://user-images.githubusercontent.com/5269168/195629261-549a3e16-dc5e-4555-b444-5177ad432a30.png)
7. Save the decision table as `discount.dmn` to your `src/main/resources` folder.

### Process modeling
8. Open the order process in the Modeler.
9. Add a task to calculate the discount before the payment invocation. Name the task **Get discount**. Change the task type to **Business Rule Task**.
10. In the Implementation section of the property panel select **DMN** as Type. Add the ID of the decision table **OrderDiscount** under **Decision reference**. Enter **discount** as the name of the **Result variable**. Select **singleEntry (TypedValue)** for **Map decision result**.
11. Add a task between the "Get discount" and "Invoke payment" tasks to apply the discount. Name the task **Apply discount**.
12. Change the task type to **Script Task**. Open the Script section. Enter **javascript** as Format (the script language). Select **Inline script** as Type. Insert the following script:
    ```javascript
    orderTotal - (orderTotal * discount / 100)
    ```
13. Name the Result variable **discountedAmount**.
14. To pay only the discounted amount, map the order total that is passed to the payment process with an Input for the "Invoke payment" task. Select the "Invoke payment" task.
15. Open the Input section. Click on the plus button (+) to add an input mapping. Enter **orderTotal** as the Local variable name. Select **String or Expression** as the Assignment type. Enter **${discountedAmount}** as the Value to map the result from the decision evaluation.

### Acceptance test
16. Start a process instance.
17. Open Cockpit and check the history of the order and the payment process. Is the discount applied correctly?

### JUnit tests
18. Add the DMN file to the test class deployments:
      ```java
      @Deployment(resources = {"payment.bpmn", "order.bpmn", "discount.dmn"})
      ```
19. Add an `orderTotal` process variable to the `testOrderProcess` test case:
      ```java
      ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("OrderProcess", "Order123",
        withVariables("orderTotal", 40.00));
      ```
20. Run the tests. They should all pass.