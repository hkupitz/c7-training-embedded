import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Deployment(resources = "payment.bpmn")
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessTests {

  @Test
  public void testCreditCardPath() {

    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<>();
    variables.put("orderTotal", 30.00);
    variables.put("customerCredit", 20.00);

    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess",
      variables);

    // Make assertions on the process instance
    assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"));
  }
}
