# Add data handling

## Goal

In this lab, we will add data handling by adding service tasks and their implementations. Then, we will adjust the process test.

## Detailed steps

1. In the process model, choose the two tasks and transform them into Service Tasks. You can achieve this by clicking on each task, selecting the wrench icon in the context and then clicking on `Service Task`.
2. After the tasks are transformed, select each task again and define an "Implementation" of the type `Delegate expression`. Enter `${deductCredit}` for the first and `${chargeCreditCard}` for the second service task.
3. In your Maven project, create a new Java class under the `io.camunda.training.delegates` package. Name it `DeductCreditDelegate`:

    ```java
    package io.camunda.training.delegates;

    import io.camunda.training.services.CustomerService;
    import org.camunda.bpm.engine.delegate.DelegateExecution;
    import org.camunda.bpm.engine.delegate.JavaDelegate;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Component;

    @Component("deductCredit")
    public class DeductCreditDelegate implements JavaDelegate {

      private final CustomerService service;

      @Autowired
      public DeductCreditDelegate(CustomerService service) {
        this.service = service;
      }

      @Override
      public void execute(DelegateExecution execution) throws Exception {

        // Extract variables from process instance
        String customerId = (String) execution.getVariable("customerId");
        Double amount = (Double) execution.getVariable("orderTotal");

        // Execute business logic using the variables
        Double openAmount = service.deductCredit(customerId, amount);
        Double customerCredit = service.getCustomerCredit(customerId);

        // Save the results to the process instance
        execution.setVariable("openAmount", openAmount);
        execution.setVariable("customerCredit", customerCredit);
      }
    }
    ```
  4. Create another Java class called `ChargeCreditCardDelegate`:
      ```java
      package io.camunda.training.delegates;

      import io.camunda.training.services.CreditCardService;
      import org.camunda.bpm.engine.delegate.DelegateExecution;
      import org.camunda.bpm.engine.delegate.JavaDelegate;
      import org.springframework.beans.factory.annotation.Autowired;
      import org.springframework.stereotype.Component;

      @Component("chargeCreditCard")
      public class ChargeCreditCardDelegate implements JavaDelegate {

        private final CreditCardService creditCardService;

        @Autowired
        public ChargeCreditCardDelegate(CreditCardService creditCardService) {
          this.creditCardService = creditCardService;
        }

        @Override
        public void execute(DelegateExecution execution) {

          // Extract variables from process instance
          String cardNumber = (String) execution.getVariable("cardNumber");
          String cvc = (String) execution.getVariable("CVC");
          String expiryData = (String) execution.getVariable("expiryDate");
          Double amount = ((Number) execution.getVariable("openAmount")).doubleValue();

          // Execute business logic using the variables
          creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
        }
      }
      ```
5. Now, we can adjust the expressions on the sequence flows to use the output data provided by the services. For the `Yes`-Path, enter `${openAmount == 0}`. For the `No`-Path, enter `${openAmount > 0}`.
6. Restart your application, re-deploy the payment process and run the process by starting a process instance via the Desktop Modeler. Provide the following variables payload:
    ```json
    {
      "customerId": { "value": "cust20" },
      "orderTotal": { "value": 40, "type": "Double" },
      "cardNumber": { "value": "1234 5678" },
      "cvc": { "value": "123" },
      "expiryDate": { "value": "08/26" }
    }
    ```
    Then, inspect the history of the instance in Cockpit.

7. Try to run your existing unit test. It should not pass.
8. We need to adjust it and register our Delegate implementations as mocks (as there is no Spring context in a JUnit test by default). Add the following method above your first test method:
   ```java
   @BeforeEach
   public void setup() {
     Mocks.register("deductCredit", new DeductCreditDelegate(new CustomerService()));
     Mocks.register("chargeCreditCard", new ChargeCreditCardDelegate(new CreditCardService()));
   }
   ```
   Then, add the missing variables to the map before starting the process and remove the line containing the `customerCredit`, which is now provided by the service:
   ```java
   variables.put("customerId", "cust20");
   variables.put("cardNumber", "1234 5678");
   variables.put("cvc", "123");
   variables.put("expiryDate", "09/26");
   ```
7. Run your unit test again. It should pass.