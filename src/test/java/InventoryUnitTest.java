import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.findId;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job;

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.community.process_test_coverage.junit5.platform7.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ProcessEngineCoverageExtension.class)
@Deployment(resources = "inventory-process.bpmn")
public class InventoryUnitTest {

  @Test
  public void testHappyPath() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("orderItemsNum", 20);
    variables.put("availableItemsNum", 20);

    ProcessInstance processInstance = runtimeService().startProcessInstanceByKey(
      "InventoryProcess", variables);

    assertThat(processInstance).isStarted();

    execute(job());

    assertThat(processInstance).isEnded()
      .hasPassed(findId("Check availability"))
      .hasPassed(findId("Reserve available items"));
  }

  @Test
  public void testCreditCardPath() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("orderItemsNum", 30);
    variables.put("availableItemsNum", 20);

    ProcessInstance processInstance = runtimeService().createProcessInstanceByKey(
        "InventoryProcess")
      .startAfterActivity(findId("Check availability"))
      .setVariables(variables)
      .execute();

    assertThat(processInstance).isStarted();
    assertThat(processInstance).isEnded()
      .hasPassed(findId("Remove unavailable items"))
      .hasPassed(findId("Reserve available items"));
  }
}