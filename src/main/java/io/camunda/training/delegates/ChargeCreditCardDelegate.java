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
    Double amount = (Double) execution.getVariable("openAmount");

    // Execute business logic using the variables
    creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
  }
}