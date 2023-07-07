package web.tutorial.backend.service;

import com.amalgamasimulation.engine.Engine;
import com.company.tutorial3.datamodel.Scenario;
import com.company.tutorial3.simulation.model.Model;
import org.springframework.stereotype.Service;
import web.tutorial.backend.dto.SimulationResultsDTO;

@Service
public class FastExperimentService {

  public SimulationResultsDTO runExperiment(Scenario scenario) {
    Model model = new Model(new Engine(), scenario);
    model.engine().setFastMode(true);
    model.engine().run(true);
    var statistics = model.getStatistics();
    return new SimulationResultsDTO(scenario.getName(),
        scenario.getTrucks().size(),
        statistics.getServiceLevel(),
        statistics.getExpenses(),
        statistics.getExpensesPerServiceLevelPercent());
  }
}
