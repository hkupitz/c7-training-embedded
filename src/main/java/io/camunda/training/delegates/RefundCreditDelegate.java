package io.camunda.training.delegates;

import io.camunda.training.services.CustomerService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("refundCredit")
public class RefundCreditDelegate implements JavaDelegate {

  private static final Logger LOG = LoggerFactory.getLogger(RefundCreditDelegate.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    LOG.info("Refunding customer credit");
  }
}
