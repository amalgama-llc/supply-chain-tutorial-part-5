package web.tutorial.backend.dto.state;

public record TruckDTO(
    String id,
    String name,
    double currentPositionX,
    double currentPositionY,
    double currentHeading,
    boolean withCargo,
    double expenses,
    double distanceTraveled) {}
