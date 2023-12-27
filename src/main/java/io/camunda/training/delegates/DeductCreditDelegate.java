package io.camunda.training.delegates;

import io.camunda.training.services.CustomerService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

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