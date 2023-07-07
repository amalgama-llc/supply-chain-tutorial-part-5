package web.tutorial.backend.service;

import com.amalgamasimulation.engine.Engine;
import com.company.tutorial3.datamodel.Scenario;
import com.company.tutorial3.simulation.ExperimentRun;
import com.company.tutorial3.simulation.model.Model;
import com.company.tutorial3.simulation.model.TransportationTask;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import web.tutorial.backend.dto.state.SimulationSpeedDTO;
import web.tutorial.backend.dto.state.SimulationStateUpdateDTO;
import web.tutorial.backend.dto.state.StatsDTO;
import web.tutorial.backend.dto.state.TaskDTO;
import web.tutorial.backend.dto.state.TruckDTO;
import web.tutorial.backend.exception.ScenarioErrorException;

@Service
public class ExperimentService {

  private static final double MIN_SIMULATION_SPEED = 1.0 / 512;
  private static final double MAX_SIMULATION_SPEED = 512;

  private Engine engine;
  private Model model;
  private ExperimentRun experimentRun;
  private Scenario scenario;
  private AtomicInteger simulationId = new AtomicInteger(0);
  private Queue<Runnable> simulationResetHandlers = new LinkedList<>();

  public void initSimulation(Scenario scenario) {
    simulationId.incrementAndGet();
    simulationResetHandlers.forEach(Runnable::run);
    simulationResetHandlers.clear();
    try {
      this.scenario = scenario;
      if (engine != null) {
        engine.stop();
        while (engine.isRunning()) {
          Thread.sleep(1000);
        }
        engine.reset();
      }
      engine = new Engine();
      engine.setFastMode(false);
      experimentRun = new ExperimentRun(scenario, engine);
      model = experimentRun.getModel();
    } catch (Exception e) {
      stopSimulation();
      throw new ScenarioErrorException(scenario.getName(), e.getMessage());
    }
  }

  public void startSimulation() {
    if (engine != null && engine.time() < engine.dateToTime(scenario.getEndDate())) {
      engine.run(false);
    }
  }

  public void stopSimulation() {
    if (engine == null) {
      return;
    }
    engine.stop();
    while (engine.isRunning()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void resetSimulation() {
    initSimulation(this.scenario);
  }

  public SimulationSpeedDTO runFaster() {
    if (engine == null) {
      return new SimulationSpeedDTO(1.0);
    }
    double newTimeScale = Math.min(MAX_SIMULATION_SPEED, engine.timeScale() * 2);
    engine.setTimeScale(newTimeScale);
    return new SimulationSpeedDTO(newTimeScale);
  }

  public SimulationSpeedDTO runSlower() {
    if (engine == null) {
      return new SimulationSpeedDTO(1.0);
    }
    double newTimeScale = Math.max(MIN_SIMULATION_SPEED, engine.timeScale() / 2);
    engine.setTimeScale(newTimeScale);
    return new SimulationSpeedDTO(newTimeScale);
  }

  public int getSimulationId() {
    return simulationId.get();
  }

  public SimulationStateUpdateDTO getSimulationState() {
    if (engine == null) {
      return null;
    }
    Long start = System.currentTimeMillis();
    SimulationStateUpdateDTO result;
    if (engine.isRunning()) {
      SynchronousQueue<SimulationStateUpdateDTO> stateUpdateObject = new SynchronousQueue<>();
      engine.visualize(start, () -> {
        try {
          var stateUpdateDTO = new SimulationStateUpdateDTO(
              getSimulationProgressPct(),
              getModelDateTime(),
              getStats(),
              getTrucksStats());
          stateUpdateObject.put(stateUpdateDTO);
        } catch (Exception e) { }
      });
      try {
        result = stateUpdateObject.take();
      } catch (Exception e) {
        return null;
      }
    } else {
      result = new SimulationStateUpdateDTO(
          getSimulationProgressPct(),
          getModelDateTime(),
          getStats(),
          getTrucksStats());
    }
    return result;
 }

  public double getModelTime() {
    return model == null ? 0.0 : model.engine().time();
  }

  private int getSimulationProgressPct() {
    if (model == null) {
      return 0;
    }
    return (int) Math.ceil(Math.min(100, 100 * engine.time() / engine.dateToTime(scenario.getEndDate())));
  }

  private LocalDateTime getModelDateTime() {
    if (engine == null) {
      return null;
    }
    return engine.timeToDate(engine.time());
  }

  private StatsDTO getStats() {
    if (engine == null) {
      return null;
    }
    return new StatsDTO(
        (int)Math.round(model.getStatistics().getServiceLevel() * 100.0),
        Math.round(model.getStatistics().getExpenses() * 100.0) / 100.0);
  }

  private List<TruckDTO> getTrucksStats() {
    if (engine == null) {
      return List.of();
    }
    return model.getTrucks()
        .stream()
        .map(truck -> new TruckDTO(	truck.getId(),
            truck.getName(),
            truck.getCurrentAnimationPoint().getX(),
            truck.getCurrentAnimationPoint().getY(),
            truck.getCurrentAnimationHeading(),
            truck.getCurrentTask() != null && truck.getCurrentTask().isMovingWithCargo(),
            Math.round(truck.getExpenses() * 100.0) / 100.0,
            Math.round(truck.getDistanceTraveled() * 100.0) / 100.0))
        .toList();
  }

  public void addSimulationResetHandler(Runnable handler) {
    simulationResetHandlers.add(handler);
  }

  public void addTaskStateChangeHandler(Consumer<TaskDTO> handler) {
    if (model != null) {
      model.addTaskStateChangeHandler(task -> handler.accept(convertToDto(task)));
    }
  }

  public void clearTaskStateChangeHandlers() {
    if (model != null) {
      model.clearTaskStateChangeHandlers();
    }
  }

  public List<TaskDTO> getAllTasks() {
    if (model == null) {
      return List.of();
    }
    return model.getTransportationTasks().stream().map(this::convertToDto).toList();
  }

  private TaskDTO convertToDto(TransportationTask task) {
    var request = task.getRequest();
    var truck = task.getTruck();
    return new TaskDTO(task.getId(),
        truck == null ? "" : truck.getName(),
        request.getSourceAsset().getName(),
        request.getDestAsset().getName(),
        engine.timeToDate(request.getCreatedTime()),
        engine.timeToDate(task.getBeginTime()),
        engine.timeToDate(request.getDeadlineTime()),
        request.isCompleted() ? engine.timeToDate(request.getCompletedTime()) : null,
        task.getStatus().toString());
  }

}
