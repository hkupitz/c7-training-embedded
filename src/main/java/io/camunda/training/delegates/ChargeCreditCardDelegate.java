package io.camunda.training.delegates;

import io.camunda.training.services.CreditCardService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.bytebuddy.pool.TypePool.Resolution.Illegal;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

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
    try {
      creditCardService.chargeAmount(cardNumber, cvc, expiryData, amount);
    } catch (IllegalArgumentException e) {
      throw new BpmnError("chargingError", "We failed to charge credit card with card number " + cardNumber, e);
    }
  }
}