package web.tutorial.backend.service;

import com.company.tutorial3.datamodel.Scenario;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import web.tutorial.backend.dto.scenario.ScenarioDTO;

@Service
public class EmbeddedScenarioProvider {

  @Autowired
  private ScenarioMapper scenarioMapper;

  public List<Scenario> readAll() {
    return readAllDTO().stream().map(scenarioMapper::readFromDTO).toList();
  }

  public List<ScenarioDTO> readAllDTO() {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

      List<ScenarioDTO> result = new ArrayList<>();
      for (var scenarioFile : resolver.getResources("/scenarios/*.json")) {
        result.add(objectMapper.readValue(scenarioFile.getURL(), ScenarioDTO.class));
      }
      Collections.sort(result, Comparator.comparing(ScenarioDTO::name));
      return result;
    } catch (IOException e) {
      e.printStackTrace();
      return List.of();
    }
  }
}
