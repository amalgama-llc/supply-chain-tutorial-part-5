package web.tutorial.backend.dto.scenario;

import java.util.List;

public record ArcDTO (String source, String dest, List<PointDTO> points) {}
