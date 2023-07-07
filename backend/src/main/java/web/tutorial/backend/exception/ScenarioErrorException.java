package web.tutorial.backend.exception;

public class ScenarioErrorException extends RuntimeException {
  private final String scenarioName;

  public ScenarioErrorException(String scenarioName, String errorMessage) {
    super(errorMessage);
    this.scenarioName = scenarioName;
  }

  public String getScenarioName() {
    return scenarioName;
  }
}
