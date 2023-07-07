package web.tutorial.backend.controller;

import com.amalgamasimulation.calendardatamodel.CalendardatamodelPackage;
import com.amalgamasimulation.ecoreutils.EcoreutilsPackage;
import com.amalgamasimulation.emf.excel.EMFExcelLoader;
import com.amalgamasimulation.emf.excel.EMFExcelTransform;
import com.amalgamasimulation.randomdatamodel.RandomdatamodelPackage;
import com.amalgamasimulation.timeseriesdatamodel.TimeseriesdatamodelPackage;
import com.company.tutorial3.datamodel.DatamodelPackage;
import com.company.tutorial3.datamodel.Scenario;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import web.tutorial.backend.dto.ExceptionDTO;
import web.tutorial.backend.dto.scenario.ScenarioDTO;
import web.tutorial.backend.dto.state.SimulationSpeedDTO;
import web.tutorial.backend.dto.state.SimulationStateUpdateDTO;
import web.tutorial.backend.dto.state.TaskDTO;
import web.tutorial.backend.exception.ScenarioErrorException;
import web.tutorial.backend.service.EmbeddedScenarioProvider;
import web.tutorial.backend.service.ExperimentService;
import web.tutorial.backend.service.ScenarioMapper;

@RestController
@RequestMapping("/")
@CrossOrigin
public class ExperimentController {

  private static final int UPDATE_ANIMATION_INTERVAL_MS = 100;

  @Autowired
  private ExperimentService experimentService;

  @Autowired
  private ScenarioMapper scenarioMapper;

  @Autowired
  private EmbeddedScenarioProvider embeddedScenarioProvider;

  @GetMapping(value = "/scenarios")
  public List<String> getScenarios() {
    return embeddedScenarioProvider.readAllDTO().stream().map(s -> s.name()).toList();
  }

  @PostMapping(value = "/init/embedded/{name}")
  public ResponseEntity<ScenarioDTO> initSimulation(@PathVariable String name) {
    List<ScenarioDTO> scenarios = embeddedScenarioProvider.readAllDTO();
    ScenarioDTO scenarioDTO = scenarios.stream().filter(s -> s.name().equals(name)).findAny().orElseThrow();
    experimentService.initSimulation(scenarioMapper.readFromDTO(scenarioDTO));
    return new ResponseEntity<>(scenarioDTO, HttpStatus.OK);
  }

  @PostMapping(value = "/init/custom")
  public ScenarioDTO initSimulationByUploadedScenario(@RequestPart("file") FilePart file) throws IOException {
    Path tempFile = Files.createTempFile(null, ".xlsx");
    file.transferTo(tempFile).subscribe();
    EMFExcelLoader<Scenario> loader = createExcelTransform().createLoader(tempFile.toString());
    loader.load();
    Scenario scenario = loader.getRootObject();
    experimentService.initSimulation(scenario);
    return scenarioMapper.convertToDTO(scenario);
  }

  private EMFExcelTransform<Scenario> createExcelTransform() {
    EMFExcelTransform<Scenario> emfExcelTransform =
        new EMFExcelTransform<Scenario>()
            .setRootClass(DatamodelPackage.eINSTANCE.getScenario())
            .addPackage(EcoreutilsPackage.eINSTANCE)
            .addPackage(CalendardatamodelPackage.eINSTANCE)
            .addPackage(RandomdatamodelPackage.eINSTANCE)
            .addPackage(TimeseriesdatamodelPackage.eINSTANCE)
        ;
    return emfExcelTransform;
  }

  @PostMapping(value = "/start")
  public void startSimulation() {
    experimentService.startSimulation();
  }

  @PostMapping(value = "/stop")
  public void stopSimulation() {
    experimentService.stopSimulation();
  }

  @PostMapping(value = "/resume")
  public void resumeSimulation() {
    experimentService.startSimulation();
  }

  @PostMapping(value = "/reset")
  public void reset() {
    experimentService.resetSimulation();
  }

  @PostMapping(value = "/faster")
  public SimulationSpeedDTO faster() {
    return experimentService.runFaster();
  }

  @PostMapping(value = "/slower")
  public SimulationSpeedDTO slower() {
    return experimentService.runSlower();
  }

  @GetMapping(value = "/updatestream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<SimulationStateUpdateDTO> getSimulationStateUpdates() {
    final int simulationId = experimentService.getSimulationId();
    return Flux	.interval(Duration.ofMillis(UPDATE_ANIMATION_INTERVAL_MS))
                .limitRate(1)
                .onBackpressureDrop()
                .takeWhile(t -> simulationId == experimentService.getSimulationId())
                .distinctUntilChanged(p -> experimentService.getModelTime())
                .flatMap(this::mapLongCounterToMonoWithSimulationState);
  }

  private Mono<SimulationStateUpdateDTO> mapLongCounterToMonoWithSimulationState(Long t) {
    return Mono .just(t)
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull(v -> experimentService.getSimulationState())
                .timeout(Duration.ofMillis(UPDATE_ANIMATION_INTERVAL_MS * 2), Mono.empty());
  }

  @GetMapping(value = "/taskstream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<TaskDTO> getTaskUpdates() {
    Flux<TaskDTO> existingTasksFlux = Flux.fromIterable(experimentService.getAllTasks());
    Flux<TaskDTO> taskUpdatesFlux = Flux .<TaskDTO>create(sink -> {
                    experimentService.addTaskStateChangeHandler(taskDto -> sendTaskMessage(taskDto, sink));
                    experimentService.addSimulationResetHandler(() -> sink.complete());
                  })
                .doFinally(signalType -> experimentService.clearTaskStateChangeHandlers());
    return existingTasksFlux.concatWith(taskUpdatesFlux);
  }

  private void sendTaskMessage(TaskDTO taskDto, FluxSink<TaskDTO> sink) {
    sink.next(taskDto);
  }

  @ExceptionHandler(ScenarioErrorException.class)
  public ResponseEntity<?> handleScenarioErrorException(ScenarioErrorException ex) {
    return new ResponseEntity<>(
	    new ExceptionDTO(ex.getScenarioName(), ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler
  public <X extends Exception> ResponseEntity<?> handleAnyException(X ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

}
