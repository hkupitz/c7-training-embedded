# Add Task Forms and Use The Camunda Tasklist

## Goal
Sometimes the error can be resolved with manual interaction. In this lab you add a user task with a simple form to your process. Additionally you have to define which data should be displayed on that forms.

## Detailed Steps
### Process modeling
1. In case the payment failed, a user should check the data and correct, if possible. If the data could not be corrected to get the payment, the process continues with the error handling and compensation.
2. Open the payment process in the Modeler. Add a task on the outgoing sequence flow of the error boundary event. Name the task like **Check failed payment data**. Change the task type to User Task.
4. In the Forms section, select **Camunda Forms** as the type. Fill the Form Reference with **checkPaymentDataForm**. This reference must become the Id of the form.
5. Add an exclusive gateway after the new user task and before the compensation throw event. Name the gateway like **Error resolvable?**. Label the outgoing sequence flow giving an answer to the question.
6. Add a condition on the sequence flow from the exclusive gateway to the compensation throw event: `${!errorResolved}`.
7. Connect the exclusive gateway with the Charge credit card task, if the data can be resolved by the user. Add a label to this sequence flow.
8. Add a condition to the sequence flow: `${errorResolved}`.

### Form modeling
9. In the Modeler, create a new Form for Camunda 7.
10. In the properties panel of the form, change the ID from the generated value to the value you have set in the user task: **checkPaymentDataForm**.
11. All (important) process data should get their own elements on the form. Additionally, a form field for the decision is required.

    Drag a Text from the palette onto the canvas to provide a headline. The Text could be like
```
### Check the failed payment
```
13. Drag a Text Field for each String type variable. Enter Field labels for the user. Enter Keys matching the process variable names.
14. Add a Number field for each Double type variable (`orderTotal` and `openAmount`).
15. Add a Checkbox field for the decision of the user. The "key" will be **errorResolved** to match the condition of the XOR gateway.
16. Mark the fields that should not be edited as "Read only"

### Run with Tasklist
16. Deploy the form from the modeler and open Tasklist at http://localhost:8080/camunda/app/tasklist.
17. Once all service tasks are completed and the payment failed, you will see a task under **All Tasks**.
18. Select the task and inspect the data on the form.
19. In case you miss some data or see any errors, you can change the form in the Modeler and redeploy the form. If you refresh the page in the tasklist the view will update to the latest form.
20. Open the Diagram tab of the form to see the highlighted task of the current process instance.
21. Switch back to the Form tab to work on the data.
22. Claim the task in the Tasklist. You can now edit the values.
23. Fix the error by editing the invalid expiry date.
24. Check the checkbox.
25. Complete the task.

### Junit tests

24. Run your unit tests. `testInvalidExpiryDate` should fail. This is because a user task is a natural wait state. Add this snippet right before it fails:
```java
// Complete the user task & let the payment fail
assertThat(processInstance).isWaitingAt(findId("Check failed payment data"));
complete(task(), withVariables("errorResolved", true));

assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
```

### Summary

In this exercise you have added a user task to the process and created a Form to display the process variables. These variables contain details of the error. The user can fix the error or mark the problem as "unresolvable".