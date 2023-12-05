# Complete Process Test Coverage

## Goal

In this lab, you will complete the process test coverage to 100%.

## Short description

* Create a new test method
* Start a process instance after the first service task
* provide it only with the required variables

## Detailed steps

1. Create a new test method. Don't forget the annotations `@Test` and `@Deployment`.
   ```java
   @Test
   @Deployment(resources = "payment_process.bpmn")
   public void testCreditSufficient(){
     // the test is written in here
   }
   ```
2. Write the test. Note that we provide the minimum of variables.
   ```java
   Map<String, Object> variables = new HashMap<>();
   variables.put("openAmount", 0);
   ProcessInstance processInstance = runtimeService()
       .createProcessInstanceByKey("PaymentProcess")
       .startAfterActivity("Activity_Deduct_Amount")
       .setVariables(variables)
       .execute();
   assertThat(processInstance)
       .isEnded()
       .hasNotPassed("Activity_Charge_Credit_Card");
   ```
3. Run the test. Inspect the test coverage in the log and the generated resources.