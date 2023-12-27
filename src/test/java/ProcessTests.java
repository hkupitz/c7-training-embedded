import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.findId;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

import io.camunda.training.delegates.ChargeCreditCardDelegate;
import io.camunda.training.delegates.DeductCreditDelegate;
import io.camunda.training.services.CreditCardService;
import io.camunda.training.services.CustomerService;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Deployment(resources = "payment.bpmn")
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessTests {

  @BeforeEach
  public void setup() {
    Mocks.register("deductCredit", new DeductCreditDelegate(new CustomerService()));
    Mocks.register("chargeCreditCard", new ChargeCreditCardDelegate(new CreditCardService()));
  }

  @Test
  public void testCreditCardPath() {

    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<>();
    variables.put("orderTotal", 30.00);
    variables.put("customerId", "cust20");
    variables.put("cardNumber", "1234 5678");
    variables.put("CVC", "123");
    variables.put("expiryDate", "09/24");

    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess",
      variables);

    // Make assertions on the process instance
    assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"));
  }
}
