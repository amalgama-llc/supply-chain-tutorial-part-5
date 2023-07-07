package web.tutorial.backend.controller;

import com.company.tutorial3.datamodel.Scenario;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.tutorial.backend.dto.scenario.ScenarioDTO;
import web.tutorial.backend.dto.SimulationResultsDTO;
import web.tutorial.backend.service.EmbeddedScenarioProvider;
import web.tutorial.backend.service.FastExperimentService;
import web.tutorial.backend.service.ScenarioMapper;

@RestController
@RequestMapping("/")
public class FastExperimentController {

  @Autowired
  private FastExperimentService fastExperimentService;

  @Autowired
  private ScenarioMapper scenarioMapper;

  @Autowired
  private EmbeddedScenarioProvider embeddedScenarioProvider;


  @GetMapping(value = "/hello")
  public String hello() {
    return "Hello, World";
  }

  @GetMapping(value = "/scenarioAnalysis")
  public List<SimulationResultsDTO> scenarioAnalysis() {
    List<Scenario> scenarios = embeddedScenarioProvider.readAll();
    return scenarios.stream().map(fastExperimentService::runExperiment).toList();
  }

  @PostMapping(value = "/runExperiment", consumes = "application/json", produces = "application/json")
  public SimulationResultsDTO runExperiment(@RequestBody ScenarioDTO scenarioDTO) {
    Scenario scenario = scenarioMapper.readFromDTO(scenarioDTO);
    return fastExperimentService.runExperiment(scenario);
  }
}
