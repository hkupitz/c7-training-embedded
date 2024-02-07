package io.camunda.training.delegates;

import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Named("paymentCompletion")
public class SendPaymentCompletionDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    execution.getProcessEngineServices()
      .getRuntimeService()
      .createMessageCorrelation("paymentCompletedMessage")
      .processInstanceBusinessKey(execution.getBusinessKey())
      .correlate();
  }
}
