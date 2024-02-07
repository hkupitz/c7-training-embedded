import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.findId;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.withVariables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.camunda.training.delegates.ChargeCreditCardDelegate;
import io.camunda.training.delegates.DeductCreditDelegate;
import io.camunda.training.delegates.SendPaymentCompletionDelegate;
import io.camunda.training.delegates.SendPaymentRequestDelegate;
import io.camunda.training.services.CreditCardService;
import io.camunda.training.services.CustomerService;
import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Deployment(resources = {"order.bpmn", "payment.bpmn"})
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessTests {

  @BeforeEach
  public void setup() {
    Mocks.register("deductCredit", new DeductCreditDelegate(new CustomerService()));
    Mocks.register("chargeCreditCard", new ChargeCreditCardDelegate(new CreditCardService()));
    Mocks.register("paymentRequest", new SendPaymentRequestDelegate());
    Mocks.register("paymentCompletion", new SendPaymentCompletionDelegate());
  }

  @Test
  public void testHappyPath() {
    Mocks.register("paymentCompletion", (JavaDelegate) execution -> {
    });

    Map<String, Object> variables = new HashMap<>();
    variables.put("openAmount", 0);

    ProcessInstance processInstance = runtimeService().createProcessInstanceByKey("PaymentProcess")
      .startAfterActivity(findId("Deduct credit"))
      .setVariables(variables)
      .execute();

    assertThat(processInstance).isEnded()
      .hasNotPassed(findId("Charge credit card"));
  }

  @Test
  public void testCreditCardPath() {
    Mocks.register("paymentCompletion", (JavaDelegate) execution -> {
    });

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

    assertThat(processInstance).isStarted();
    execute(job());

    assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
    execute(job());

    // Make assertions on the process instance
    assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"));
  }

  @Test
  public void testInvalidExpiryDate() {
    Mocks.register("paymentCompletion", (JavaDelegate) execution -> {});

    // Create a HashMap to put in variables for the process instance
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("orderTotal", 30.00);
    variables.put("customerId", "cust20");
    variables.put("cardNumber", "1234 5678");
    variables.put("CVC", "789");
    variables.put("expiryDate", "09/24x");

    // Start process with Java API and variables
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess",
      variables);

    assertThat(processInstance).isStarted();
    execute(job());

    // try to execute credit card payment
    assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
    RuntimeException exception = assertThrows(IllegalArgumentException.class, () -> execute(job()));
    assertEquals(exception.getMessage(), "Invalid expiry date");
  }

  @Test
  public void testOrderProcess() {
    Mocks.register("paymentRequest", (JavaDelegate) execution -> {});

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("OrderProcess",
      "Test 1", withVariables(
        "orderTotal", 30.00,
        "customerId", "cust30",
        "cardNumber", "1234 5678",
        "CVC", "123",
        "expiryDate", "09/24"
      ));

    runtimeService().correlateMessage("paymentCompletedMessage");
    assertThat(processInstance).isEnded();
  }
}
