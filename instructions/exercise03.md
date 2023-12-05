# Enhance with variable handling

## Goal

In this lab, we will improve the variable handling by adding service tasks and their implementations. Then, we will adjust the Junit test

## Short description

* Morph tasks to be service tasks
* Bind service tasks to implementations
* Create implementations and bind them to the context
* Adjust conditional expressions on sequence flows
* Adjust the Junit test to use the new variable handling

## Detailed steps

1. In the process model, choose the 2 tasks and morph them to be service tasks. You can achieve this by clicking on each task, selecting the wrench icon in the context and then select `Service Task`.
2. After the tasks are morphed, select each task again and define an implementation of type `Delegate expression`. Name one `${deductCredit}` and the other one `${chargeCreditCard}`.
3. In your Java project, create a new Java class. Name it `DeductCreditDelegate`:
   ```java
   package org.camunda.training;

   import com.camunda.training.services.CustomerService;
   import org.camunda.bpm.engine.delegate.DelegateExecution;
   import org.camunda.bpm.engine.delegate.JavaDelegate;
   
   import javax.inject.Inject;
   import javax.inject.Named;
   
   @Named("deductCredit")
   public class DeductCreditDelegate implements JavaDelegate {
     private final CustomerService service;
   
     @Inject
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
4. Create another Java class right next to it called `ChargeCreditCardDelegate`:
   ```java
   package org.camunda.training;

   import org.camunda.bpm.engine.delegate.DelegateExecution;
   import org.camunda.bpm.engine.delegate.JavaDelegate;
   import org.camunda.training.services.CreditCardService;
   
   import javax.inject.Inject;
   import javax.inject.Named;
   
   @Named("chargeCreditCard")
   public class ChargeCreditCardDelegate implements JavaDelegate {
     private final CreditCardService creditCardService;
   
     @Inject
     public ChargeCreditCardDelegate(CreditCardService creditCardService) {
       this.creditCardService = creditCardService;
     }
   
     @Override
     public void execute(DelegateExecution execution) {

       // Extract variables from process instance
       String cardNumber = (String) execution.getVariable("cardNumber");
       String cvc = (String) execution.getVariable("CVC");
       String expiryData = (String) execution.getVariable("expiryDate");
       Double amount = (Double) execution.getVariable("openAmount");

       // Execute business logic using the variables
       creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
     }
   }
   ```
5. Now, we can adjust the expressions on the sequence flows to use the data provided by the services. For the `yes`-Path, enter `${openAmount == 0}`. For the `no`-Path, enter `${openAmount > 0}`.
6. Finally, we will have to adjust the Junit test. There, we need to register our delegate implementations as mocks (there is no spring context in a junit test by default). Add this method above your first test method:
   ```java
   @BeforeEach
   public void setup() {
     Mocks.register("deductCredit", new DeductCreditDelegate(new CustomerService()));
     Mocks.register("chargeCreditCard", new ChargeCreditCardDelegate(new CreditCardService()));
   }
   ```
   Then, add more variables to the map before starting the process and remove the line containing the `customerCredit`:
   ```java
   variables.put("customerId", "cust20");
   variables.put("cardNumber", "1234 5678");
   variables.put("CVC","123");
   variables.put("expiryDate","09/24");
   ```
7. Run your unit test and inspect the log output.
8. Optional: Restart your application and run the process by starting a process from tasklist. Then, inspect the history of the process instance.