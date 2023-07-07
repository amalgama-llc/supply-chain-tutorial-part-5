package web.tutorial.backend.dto.scenario;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record ScenarioDTO(String name,
                          @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
                          LocalDateTime beginDate,
                          @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
                          LocalDateTime endDate,
                          double intervalBetweenRequestHrs,
                          double maxDeliveryTimeHrs,
                          List<TruckDTO> trucks,
                          List<NodeDTO> nodes,
                          List<ArcDTO> arcs,
                          List<WarehouseDTO> warehouses,
                          List<StoreDTO> stores
) {}
