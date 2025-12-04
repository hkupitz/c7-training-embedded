# Add Task Forms and Use The Camunda Tasklist

## Goal
Sometimes an error can be resolved with manual interaction. In this lab you add a user task with a simple form to your process.

## Detailed Steps
### Process modeling
1. In case the payment failed, a user should check the data and correct it, if possible. If the data could not be corrected, the process continues with the error handling and compensation.
2. Open the payment process in the Modeler. Before the compensation event, add a **User Task** on the outgoing sequence flow of the error boundary event. Name the task **Check failed payment data**. Change the task type to User Task.
3. In the Forms section of the property panel, select **Camunda Forms** as the Type. Fill the Form reference field with **checkPaymentDataForm**. This reference is the ID of the form you are going to create.
4. Add an exclusive gateway after the new user task and before the compensation throw event. Label the gateway **Error resolved?**. Label the outgoing sequence flow giving an answer to the question.
5. Add a condition on the sequence flow from the exclusive gateway to the compensation throw event: `${!errorResolved}`.
6. Connect the exclusive gateway with the "Charge credit card" task, checking if the user marked the error as resolved. Add a label to this sequence flow.
7. Add a condition to the sequence flow: `${errorResolved}`.

### Form modeling
8. In the Modeler, create a new Form for Camunda 7.
9. In the property panel of the form, under **General**, change the ID from the generated value to the reference you have set in the user task: **checkPaymentDataForm**.
10. All (important) process data should get their own elements on the form. Additionally, a form field for the decision is required.
11. Drag a **Text view** from the palette onto the canvas to provide a headline. The Text could be something like
    ```
    ### Check the failed payment
    ```
12. Drag a **Text field** for each String type variable. Enter a **Field label** for the user. Enter a **Key** each matching the process variable names.
13. Add a **Number** field for each Double type variable (`orderTotal` and `openAmount`).
14. Add a **Checkbox** field for the decision of the user. The "key" will be **errorResolved** to match the condition of the XOR gateway.
15. Mark the fields that should not be edited as "Read only".

### Run with Tasklist
16. Save the form as "check-payment-data.form".
17. Deploy the form from the Modeler.
18. Start a process instance of the order process with the payload from exercise 7.
19. Open the Camunda Tasklist.
20. Once all service tasks are completed and the payment failed, you will see a task under **All tasks** on the left side after reloading the page.
21. Select the task and inspect the data on the form.
22. In case you miss some data or see any errors, you can change the form in the Modeler and re-deploy the form. If you refresh the page in the Tasklist the view will update to the latest form.
23. Open the Diagram tab of the form to see the highlighted task of the current process instance.
24. Switch back to the Form tab to work on the data.
25. Claim the task in the Tasklist. You can now edit the values.
26. Fix the error by correcting the invalid expiry date.
27. Check the checkbox.
28. Complete the task.

### JUnit tests

24. Run your unit tests. `testInvalidExpiryDate` should fail. This is because a user task is a natural wait state. Add this snippet right before it fails:
    ```java
    // Complete the user task & let the payment fail
    assertThat(processInstance).isWaitingAt(findId("Check failed payment data"));
    complete(task(), withVariables("errorResolved", false));
    ```