import io.camunda.training.delegates.ChargeCreditCardDelegate;
import io.camunda.training.delegates.DeductCreditDelegate;
import io.camunda.training.services.CreditCardService;
import io.camunda.training.services.CustomerService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.assertj.core.api.Assertions.*;

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

    // Create a HashMap for the variables payload
    Map<String, Object> variables = new HashMap<>();
    variables.put("orderTotal", 30.00);
    variables.put("customerId", "cust20");
    variables.put("cardNumber", "1234 5678");
    variables.put("cvc", "123");
    variables.put("expiryDate", "09/26");

    // Start process via Java API
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);

    // Make assertions on the process instance
    assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"));
  }

  @Test
  public void testCreditSufficientPath() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("openAmount", 0);

    ProcessInstance processInstance = runtimeService()
            .createProcessInstanceByKey("PaymentProcess")
            .startAfterActivity(findId("Deduct credit"))
            .setVariables(variables)
            .execute();

    assertThat(processInstance)
            .isEnded()
            .hasNotPassed(findId("Charge credit card"));
  }
}
