package io.camunda.training.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("completePayment")
public class CompletePaymentDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    // Send a message back to the order process to continue its execution
    execution.getProcessEngineServices()
            .getRuntimeService()
            .createMessageCorrelation("paymentCompletionMessage")
            .processInstanceBusinessKey(execution.getBusinessKey())
            .correlate();
  }
}
