package web.tutorial.backend.dto.state;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record SimulationStateUpdateDTO (
    Integer simulationProgressPct,
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime modelDateTime,
    StatsDTO stats,
    List<TruckDTO> truckStats
){}
