import { useEffect, useState } from "react";
import axios from "axios";
import { numberWithSpaces } from "./utils";
import ScenarioList from "./components/ScenarioList";
import SimulationControls from "./components/SimulationControls";
import SummaryStatistics from "./components/SummaryStatistics"; 
import MainPanel from "./components/MainPanel";
import Badge from "react-bootstrap/Badge";

function App() {
  const MAIN_URL = process.env.REACT_APP_API_URL;
  const INITIAL_TIME = "";

  const [tasks, setTasks] = useState(new Map());
  const [gantts, setGantts] = useState([]);
  const [modelDateTime, setModelDateTime] = useState(INITIAL_TIME);
  const [simulationProgressPct, setSimulationProgressPct] = useState(0);
  const [stats, setStats] = useState({
    serviceLevelPct: undefined,
    expenses: undefined,
  });
  const [trucks, setTrucks] = useState([]);
  const [nodes, setNodes] = useState(new Map());
  const [arcs, setArcs] = useState([]);
  const [stores, setStores] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [timeScale, setTimeScale] = useState(1);

  const [blocking, setBlocking] = useState();

  const [simulationState, setSimulationState] = useState("no_scenario")   //  "no_scenario", "ready_to_run", "running", "finished"

  const [embeddedScenarioNames, setEmbeddedScenarioNames] = useState();

  const [currentScenarioName, setCurrentScenarioName] = useState();
  const [currentScenarioType, setCurrentScenarioType] = useState();   // "embedded", "custom"
  const [errorMsg, setErrorMsg] = useState();

  let taskEventStream;
  let updateEventStream;

  useEffect(() => {
    let loading = true;
    if (loading) {
      loading = false;
      const initScenario = () => {
        axios({ url: MAIN_URL + "/scenarios", method: "GET" })
          .then((response) => {
            setEmbeddedScenarioNames(response.data);
            let firstScenarioName = response.data[0];
            onEmbeddedScenarioSelected(firstScenarioName);
          })
          .then(() => {});
      };
      initScenario();
    }
  }, []);
  
  const processUpdateStreamMessage = (event) => {
    const jsonData = JSON.parse(event.data);
    const progressPct = jsonData.simulationProgressPct;
    if (progressPct === 100) {
      setSimulationState("finished");
    }
    setSimulationProgressPct(progressPct)
    setModelDateTime(jsonData.modelDateTime);
    setStats({
      ...stats,
      serviceLevelPct: jsonData.stats.serviceLevelPct,
      expenses: jsonData.stats.expenses,
    });
    const newTrucks = [];
    jsonData.truckStats.forEach((element) => {
      const truck = {
        id: element.id,
        name: element.name,
        x: element.currentPositionX,
        y: element.currentPositionY,
        heading: element.currentHeading,
        withCargo: element.withCargo,
        expenses: element.expenses,
        distanceTraveled: element.distanceTraveled,
      };
      newTrucks.push(truck);
    });
    setTrucks(newTrucks);
  };

  const format = (taskEvent) => {
    const data = [
      "Truck " + taskEvent.truck,
      taskEvent.id,
      new Date(parseDateTime(taskEvent.started)),
      new Date(parseDateTime(taskEvent.completed)),
    ];
    return data;
  };
  
  const parseDateTime = (str) => {
    return Date.parse(str);
  }

  const processTaskStreamMessage = (event) => {
    const taskData = JSON.parse(event.data);
    setTasks((prevTasks) => new Map(prevTasks).set(taskData.id, taskData));
    if (taskData.status.startsWith("COMPLETE")) {
      setGantts((prevGantts) => [...prevGantts, format(taskData)])
    }
  };

  const startSimulation = () => {
    if (updateEventStream) {
      updateEventStream.close();
    }
    updateEventStream = new EventSource(MAIN_URL + "/updatestream");
    updateEventStream.onmessage = processUpdateStreamMessage;
    updateEventStream.onerror = () => {
      updateEventStream.close();
    };

    axios({ url: MAIN_URL + "/start", method: "POST" });
    if (taskEventStream) {
      taskEventStream.close();
    }
    taskEventStream = new EventSource(MAIN_URL + "/taskstream");
    taskEventStream.onmessage = processTaskStreamMessage;
    taskEventStream.onerror = (err) => {
      console.error("simulation failed:", err);
      taskEventStream.close();
    };
    setSimulationState("running");
  }

  const stopSimulation = () => {
    axios({ url: MAIN_URL + "/stop", method: "POST" }).then(() => setSimulationState("ready_to_run"));
  }

  const resumeSimulation = () => {
    axios({ url: MAIN_URL + "/resume", method: "POST" }).then(() => setSimulationState("running"));
  }

  const resetSimulation = () => {
    axios({ url: MAIN_URL + "/reset", method: "POST" }).then(() => reset());
  }

  const simulateSlower = () => {
    axios({ url: MAIN_URL + "/slower", method: "POST" }).then((response) => {
      if (response.status === 200) {
        setTimeScale(response.data.simulationSpeed)
      }
    });
  }

  const simulateFaster = () => {
    axios({ url: MAIN_URL + "/faster", method: "POST" }).then((response) => {
      if (response.status === 200) {
        setTimeScale(response.data.simulationSpeed)
      }
    });
  }

  const onScenarioDtoReceivedFromServer = (scenario, scenarioType) => {
    reset();
    setModelDateTime(scenario.beginDate);
    setArcs(scenario.arcs);
    let nodesMap = new Map();
    scenario.nodes.forEach((node) => nodesMap.set(node.id, node));
    setNodes(nodesMap);
    setStores(scenario.stores);
    setWarehouses(scenario.warehouses);
    setBlocking(false);
    setErrorMsg();
    setCurrentScenarioName(scenario.name);
    setCurrentScenarioType(scenarioType);
  }

  const onEmbeddedScenarioSelected = (scenarioName) => {
    if (scenarioName) {
      axios({ url: MAIN_URL + "/init/embedded/" + scenarioName, method: "POST" })
        .then((response) => onScenarioDtoReceivedFromServer(response.data, "embedded"))
        .catch((response) => {
          if (response.data) {
            setErrorMsg(response.data.message);
            if (response.data.scenarioName === scenarioName) {
              setBlocking(true);
            }
          }
        });
      setSimulationState("ready_to_run");
    }
  }

  const onCustomScenarioFileSelected = (scenarioFile) => {
    let formData = new FormData();
    formData.append("file", scenarioFile);
    axios.post(MAIN_URL + "/init/custom", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      }
    })
      .then((response) => onScenarioDtoReceivedFromServer(response.data, "custom"))
      .catch((response) => {
        if (response.data) {
          setErrorMsg(response.data.message);
          setBlocking(true);
        }
      });
  }

  const reset = () => {
    setTasks(new Map());
    setSimulationProgressPct(0);
    setModelDateTime(INITIAL_TIME);
    setTimeScale(1);
    setStats({
      ...stats,
      serviceLevelPct: undefined,
      expenses: undefined,
    });
    setGantts([]);
    setTrucks([]);
    setSimulationState("ready_to_run");
  };

  return (
    <>
      <div className="box-scenarios">
        <h4>Supply Chain Simulation</h4>

        {errorMsg && (
          <Badge className="error-message" bg="danger">
            {errorMsg}
          </Badge>
        )}

        <ScenarioList currentScenarioName={currentScenarioName}
                      currentScenarioType={currentScenarioType}
                      embeddedScenarioNames={embeddedScenarioNames}
                      onEmbeddedScenarioSelected={onEmbeddedScenarioSelected}
                      onCustomScenarioFileSelected={onCustomScenarioFileSelected} />
      </div>
      <div className="box-simulationcontrols">
      <SimulationControls modelDateTime={modelDateTime}
                              simulationProgressPct={simulationProgressPct}
                              timeScale={timeScale}
                              simulationState={simulationState}
                              onStart={startSimulation}
                              onStop={stopSimulation}
                              onResume={resumeSimulation}
                              onReset={resetSimulation}
                              onSlower={simulateSlower}
                              onFaster={simulateFaster}
                              blocking={blocking} />
      </div>
      <div className="box-statistics">
      <SummaryStatistics  stats={stats} 
                              numberWithSpaces={numberWithSpaces} />  
      </div>
      <div className="box-mainpanel">
        <MainPanel
          arcs={arcs}
          nodes={nodes}
          stores={stores}
          warehouses={warehouses}
          trucks={trucks}
          gantts={gantts}
          tasks={[...tasks.values()]}
            />
      </div>
    </>
  );
}

export default App;
