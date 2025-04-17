# Introduce compensation

## Goal

In this lab, we will include compensation to show that a task is compensated after a business error happened

## Detailed steps

1. Currently, you should have an **Error Boundary Event** on your **Charge credit card** task leading to an **End Event**.
2. Add an **Intermediate Compensation Throw Event** after the **Error Boundary Event**.
3. Now, add a **Boundary Event** to the **Deduct credit** task.
4. Transform the **Boundary Event** to a **Compensate Boundary Event**.
5. In the context of this **Compensate Boundary Event**, create a Task. This task will not be connected with a sequence flow, but with an **Association** (which is correct and wanted). Name this task **Refund credit**.
6. Add a new Java Delegate that prints a log in case of a refund.
6. Deploy and test the process with an appropriate payload on the real engine. The history in cockpit should show that the compensation was triggered.
6. Now go to your test method `testInvalidExpiryDate`. Add another statement to the last assert statement so that the whole statement will look like this:
    ```java
   assertThat(processInstance)
        .isEnded()
        .hasPassed(findId("Charge credit card"))
        .hasNotPassed("Payment completed ID")
        .hasPassed("Compensation Boundary Event ID")
        .hasPassed("Compensation Task ID");
    ```
7. Run your tests.