import io.camunda.training.delegates.ChargeCreditCardDelegate;
import io.camunda.training.delegates.DeductCreditDelegate;
import io.camunda.training.delegates.CompletePaymentDelegate;
import io.camunda.training.delegates.InvokePaymentDelegate;
import io.camunda.training.services.CreditCardService;
import io.camunda.training.services.CustomerService;
import org.camunda.bpm.engine.delegate.JavaDelegate;
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

@Deployment(resources = {"payment.bpmn", "order.bpmn"})
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ProcessTests {

  @BeforeEach
  public void setup() {
    Mocks.register("deductCredit", new DeductCreditDelegate(new CustomerService()));
    Mocks.register("chargeCreditCard", new ChargeCreditCardDelegate(new CreditCardService()));
    Mocks.register("invokePayment", new InvokePaymentDelegate());
    Mocks.register("completePayment", new CompletePaymentDelegate());
  }

  @Test
  public void testCreditCardPath() {
    Mocks.register("completePayment", (JavaDelegate) ex -> {});

    // Create a HashMap for the variables payload
    Map<String, Object> variables = new HashMap<>();
    variables.put("orderTotal", 30.00);
    variables.put("customerId", "cust20");
    variables.put("cardNumber", "1234 5678");
    variables.put("cvc", "123");
    variables.put("expiryDate", "09/26");

    // Start process via Java API
    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);

    // Execute asynchronous continuation (as the job executor is disabled during unit tests)
    assertThat(processInstance).isStarted();
    execute(job());

    assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
    execute(job());

    // Make assertions on the process instance
    assertThat(processInstance).isEnded().hasPassed(findId("Charge credit card"));
  }

  @Test
  public void testCreditSufficientPath() {
    Mocks.register("completePayment", (JavaDelegate) ex -> {});

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

  @Test
  public void testInvalidExpiryDate(){
    Mocks.register("completePayment", (JavaDelegate) execution -> {});
    Mocks.register("refundCredit", (JavaDelegate) execution -> {});

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("orderTotal", 30.00);
    variables.put("customerId", "cust20");
    variables.put("cardNumber", "1234 5678");
    variables.put("cvc", "789");
    variables.put("expiryDate", "09/26x");

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("PaymentProcess", variables);

    // Execute start event job
    assertThat(processInstance).isWaitingAt(findId("Payment requested"));
    execute(job());

    // Execute service task job
    assertThat(processInstance).isWaitingAt(findId("Charge credit card"));
    execute(job());

    // Complete external compensation task
    complete(externalTask());

    assertThat(processInstance).isEnded()
            .hasPassed(findId("Charge credit card"))
            .hasNotPassed("Payment_PaymentCompletedEndEvent")
            .hasPassed("Payment_InvalidExpiryDateCompensationEvent")
            .hasPassed("Payment_RefundCreditTask");
  }

  @Test
  public void testOrderProcess() {

    // Not starting the payment process for this test
    Mocks.register("invokePayment", (JavaDelegate) execution -> {
    });

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("OrderProcess", "Order123");

    runtimeService().correlateMessage("paymentCompletionMessage");
    assertThat(processInstance).isEnded();
  }

  @Test
  public void testEndToEnd(){
    ProcessInstance orderProcessInstance = runtimeService().startProcessInstanceByKey(
      "OrderProcess", "Test 1",
      withVariables("orderTotal", 30.00,
              "customerId", "cust30",
              "cardNumber", "1234 5678",
              "cvc", "123",
              "expiryDate", "09/26"
      )
    );

    assertThat(orderProcessInstance).isWaitingAt("Order_Event_PaymentCompleted");

    ProcessInstance paymentProcessInstance = processInstanceQuery().processDefinitionKey("PaymentProcess").singleResult();

    assertThat(paymentProcessInstance).isWaitingAt(findId("Payment requested"));
    execute(job());

    assertThat(paymentProcessInstance).isEnded();
    assertThat(orderProcessInstance).isEnded();
  }
}
