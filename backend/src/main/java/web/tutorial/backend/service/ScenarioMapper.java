package web.tutorial.backend.service;

import com.amalgamasimulation.randomdatamodel.Distribution;
import com.amalgamasimulation.randomdatamodel.ExponentialDistribution;
import com.amalgamasimulation.randomdatamodel.RandomdatamodelFactory;
import com.company.tutorial3.datamodel.Arc;
import com.company.tutorial3.datamodel.DatamodelFactory;
import com.company.tutorial3.datamodel.Node;
import com.company.tutorial3.datamodel.Scenario;
import com.company.tutorial3.datamodel.Store;
import com.company.tutorial3.datamodel.Truck;
import com.company.tutorial3.datamodel.Warehouse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import web.tutorial.backend.dto.scenario.ArcDTO;
import web.tutorial.backend.dto.scenario.NodeDTO;
import web.tutorial.backend.dto.scenario.PointDTO;
import web.tutorial.backend.dto.scenario.ScenarioDTO;
import web.tutorial.backend.dto.scenario.StoreDTO;
import web.tutorial.backend.dto.scenario.TruckDTO;
import web.tutorial.backend.dto.scenario.WarehouseDTO;

@Service
public class ScenarioMapper {

  public Scenario readFromDTO(ScenarioDTO scenarioDTO) {
    Scenario scenario = DatamodelFactory.eINSTANCE.createScenario();

    scenario.setName(scenarioDTO.name());
    scenario.setBeginDate(scenarioDTO.beginDate());
    scenario.setEndDate(scenarioDTO.endDate());

    var intervalBetweenRequests = RandomdatamodelFactory.eINSTANCE.createExponentialDistribution();
    intervalBetweenRequests.setMean(scenarioDTO.intervalBetweenRequestHrs());
    scenario.setIntervalBetweenRequestsHrs(intervalBetweenRequests);

    scenario.setMaxDeliveryTimeHrs(scenarioDTO.maxDeliveryTimeHrs());

    Map<String, Node> nodes = new HashMap<>();

    scenarioDTO.nodes().forEach(nodeDTO -> {
      String id = nodeDTO.id();
      var node = DatamodelFactory.eINSTANCE.createNode();
      node.setScenario(scenario);
      node.setId(id);
      node.setX(nodeDTO.x());
      node.setY(nodeDTO.y());
      nodes.put(id, node);
    });

    scenarioDTO.arcs().forEach(arcDTO -> {
      Node source = nodes.get(arcDTO.source());
      Node dest = nodes.get(arcDTO.dest());
      if (source.equals(dest)) {
        String message = "Arc source and destination should be different Nodes.\n";
        System.err.println(message);
        throw new RuntimeException(message);
      }
      var arc = DatamodelFactory.eINSTANCE.createArc();
      arc.setScenario(scenario);
      arc.setSource(source);
      arc.setDest(dest);
      for (var pointDto : arcDTO.points()) {
        var point = DatamodelFactory.eINSTANCE.createPoint();
        point.setX(pointDto.x());
        point.setY(pointDto.y());
        point.setArc(arc);
      }
    });

    scenarioDTO.trucks().forEach(truckDTO -> {
      var truck = DatamodelFactory.eINSTANCE.createTruck();
      truck.setScenario(scenario);
      truck.setId(truckDTO.id());
      truck.setName(truckDTO.name());
      truck.setSpeed(truckDTO.speed());
      truck.setInitialNode(nodes.get(truckDTO.initialNode()));
    });

    scenarioDTO.warehouses().forEach(whDTO -> {
      var wh = DatamodelFactory.eINSTANCE.createWarehouse();
      wh.setScenario(scenario);
      wh.setId(whDTO.id());
      wh.setName(whDTO.name());
      wh.setNode(nodes.get(whDTO.node()));
    });

    scenarioDTO.stores().forEach(storeDTO -> {
      var store = DatamodelFactory.eINSTANCE.createStore();
      store.setScenario(scenario);
      store.setId(storeDTO.id());
      store.setName(storeDTO.name());
      store.setNode(nodes.get(storeDTO.node()));
    });

    return scenario;
  }

  public ScenarioDTO convertToDTO(Scenario scenario) {
    Distribution intervalBetweenRequestsDistr = scenario.getIntervalBetweenRequestsHrs();
    double intervalBetweenRequestsHrs;
    if (intervalBetweenRequestsDistr instanceof ExponentialDistribution exponentialDistribution) {
      intervalBetweenRequestsHrs = exponentialDistribution.getMean();
    } else {
      intervalBetweenRequestsHrs = 1.0;
    }
    return new ScenarioDTO(
        scenario.getName(),
        scenario.getBeginDate(),
        scenario.getEndDate(),
        intervalBetweenRequestsHrs,
        scenario.getMaxDeliveryTimeHrs(),
        scenario.getTrucks().stream().map(this::convertToDTO).toList(),
        scenario.getNodes().stream().map(this::convertToDTO).toList(),
        scenario.getArcs().stream().map(this::convertToDTO).toList(),
        scenario.getWarehouses().stream().map(this::convertToDTO).toList(),
        scenario.getStores().stream().map(this::convertToDTO).toList());
  }

  private TruckDTO convertToDTO(Truck truck) {
    return new TruckDTO(truck.getId(), truck.getName(), truck.getSpeed(), truck.getInitialNode().getId());
  }

  private NodeDTO convertToDTO(Node node) {
    return new NodeDTO(node.getId(), node.getX(), node.getY());
  }

  private ArcDTO convertToDTO(Arc arc) {
    List<PointDTO> points = arc.getPoints().stream().map(p -> new PointDTO(p.getX(), p.getY())).toList();
    return new ArcDTO(arc.getSource().getId(), arc.getDest().getId(), points);
  }

  private WarehouseDTO convertToDTO(Warehouse warehouse) {
    return new WarehouseDTO(warehouse.getId(), warehouse.getName(), warehouse.getNode().getId());
  }

  private StoreDTO convertToDTO(Store store) {
    return new StoreDTO(store.getId(), store.getName(), store.getNode().getId());
  }

}
