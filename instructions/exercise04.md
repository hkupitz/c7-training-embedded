# Complete Process Test Coverage

## Goal

In this lab, you will complete the process test coverage, so it becomes 100%.

## Detailed steps

1. Create a new test method called `testCreditSufficientPath()`. Don't forget the `@Test` annotation.
   ```java
   @Test
   public void testCreditSufficientPath() {
     
    ...

   }
   ```
2. Write the test. Note that we provide the minimum of variables.
   ```java
   Map<String, Object> variables = new HashMap<>();
   variables.put("openAmount", 0);

   ProcessInstance processInstance = runtimeService()
       .createProcessInstanceByKey("PaymentProcess")
       .startAfterActivity(findId("Deduct credit"))
       .setVariables(variables)
       .execute();

   assertThat(processInstance)
       .isEnded()
       .hasNotPassed(findId("Charge credit card"));
   ```
3. Run the test.
4. Inspect the test coverage in the log and the generated report.