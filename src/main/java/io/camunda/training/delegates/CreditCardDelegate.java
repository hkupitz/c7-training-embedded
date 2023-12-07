package io.camunda.training.delegates;

import io.camunda.training.services.CreditCardService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("creditCardDelegate")
public class CreditCardDelegate implements JavaDelegate {

  private CreditCardService service;

  public CreditCardDelegate(@Autowired CreditCardService service) {
    this.service = service;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String cardNumber = (String) execution.getVariable("cardNumber");
    String expiryDate = (String) execution.getVariable("expiryDate");
    String cvc = (String) execution.getVariable("cvc");
    Double openAmount = (Double) execution.getVariable("openAmount");

    if (service.validateExpiryDate(expiryDate)) {
      service.chargeAmount(cardNumber, cvc, expiryDate, openAmount);
    } else {
      throw new IllegalArgumentException("Invalid expiry date: " + expiryDate);
    }
  }
}
