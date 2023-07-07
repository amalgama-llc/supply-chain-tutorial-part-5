import React from "react";
import Button from 'react-bootstrap/Button';
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import ProgressBar from 'react-bootstrap/ProgressBar';
import { formatDateTime } from "../utils";

const formatTimeScale = (val) => {
  if (val >= 1) {
    return val + " x";
  } else {
    return "1/" + 1 / val + " x";
  }
}

function SimulationControls({modelDateTime, simulationProgressPct, timeScale, simulationState, blocking,
  onStart, onStop, onResume, onReset, onSlower, onFaster}) {

    const stateMapping = {"no_scenario": "Please set a scenario", 
                          "ready_to_run" : "Ready to run",
                          "running" : "Running",
                          "finished": "Finished"};
    return (
    <div>
        <h5>Simulation</h5>

        <ProgressBar animated={simulationProgressPct < 100} now={simulationProgressPct} label={`${simulationProgressPct}%`}></ProgressBar>

        <div className="mb-2 mt-2">
          <label style={{width: "6em"}}>Model time:</label>
          {modelDateTime ? formatDateTime(modelDateTime) : " <press Start>"}
        </div>
        <div className="mb-2 mt-2">
          <label style={{width: "6em"}}>Status:</label>
          {stateMapping[simulationState]}
        </div>
        <div className="mb-2 mt-2">
          <label style={{width: "6em"}}>Speed:</label>
          <label style={{width: "4em", textAlign: "left"}}>{formatTimeScale(timeScale)}</label>
          <ButtonGroup>
            <Button variant="outline-primary" size="sm" onClick={(e) => onSlower()}>
              Slower
            </Button>          
            <Button variant="outline-primary" size="sm" onClick={(e) => onFaster()}>
              Faster
            </Button>
          </ButtonGroup>
        </div>

        <button
          className="large-button deep-blue"
          disabled={simulationProgressPct > 0 || simulationState !== "ready_to_run" || blocking}
          onClick={(e) => onStart()}
        >
          START
        </button>
        <button
          className="large-button deep-blue"
          disabled={simulationProgressPct === 0 || simulationState !== "ready_to_run" || blocking}
          onClick={(e) => onResume()}
        >
          RESUME
        </button>
        <button
          className="large-button deep-blue"
          disabled={simulationState !== "running" || blocking}
          onClick={(e) => onStop()}
        >
          STOP
        </button>
        <button
          className="large-button yellow"
          disabled={simulationState === "no_scenario" || blocking}
          onClick={(e) => onReset()}
        >
          RESET
        </button>
        <br></br>
    </div>
    );
}

export default SimulationControls;