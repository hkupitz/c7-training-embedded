package io.camunda.training.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("invokePayment")
public class InvokePaymentDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    // Generate unique business key
    String orderId = UUID.randomUUID().toString();

    // Invoke payment process via message and pass on all variables
    ProcessInstance processInstance = execution
            .getProcessEngineServices()
            .getRuntimeService()
            .createMessageCorrelation("paymentRequestMessage")
            .setVariables(execution.getVariables())
            .processInstanceBusinessKey(orderId)
            .correlateStartMessage();

    // Store business key for order process as well
    execution.setProcessBusinessKey(orderId);
  }
}
