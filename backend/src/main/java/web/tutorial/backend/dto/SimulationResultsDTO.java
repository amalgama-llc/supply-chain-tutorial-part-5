package web.tutorial.backend.dto;

public record SimulationResultsDTO(String scenarioName,
                                   int trucksCount,
                                   double serviceLevel,
                                   double expenses,
                                   double expensesToServiceLevel
) {}