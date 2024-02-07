package io.camunda.training.delegates;

import jakarta.inject.Named;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;

@Named("paymentRequest")
public class SendPaymentRequestDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    ProcessInstance processInstance = execution
      .getProcessEngineServices()
      .getRuntimeService()
      .createMessageCorrelation("paymentRequestedMessage")
      .setVariables(execution.getVariables())
      .processInstanceBusinessKey(execution.getProcessBusinessKey())
      .correlateStartMessage();
    execution.setVariable("paymentProcessInstanceId", processInstance.getId());
  }
}
