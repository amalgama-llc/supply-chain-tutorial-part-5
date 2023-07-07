import React from "react";
import Form from "react-bootstrap/Form";
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Badge from 'react-bootstrap/Badge';

function ScenarioList({currentScenarioName, currentScenarioType, embeddedScenarioNames, onEmbeddedScenarioSelected, onCustomScenarioFileSelected}) {

  return (
    <>
    To set up a simulation scenario:<br></br>
    <DropdownButton id="scenario-list" title="Select an embedded scenario" className="ms-2">
      {embeddedScenarioNames &&
            embeddedScenarioNames.map((scenarioName) => (
              <Dropdown.Item key={scenarioName} onClick={(e) => onEmbeddedScenarioSelected(scenarioName)}>{scenarioName}</Dropdown.Item>
            ))}
    </DropdownButton>

    <Form.Group controlId="formFile" className="mb-2 mt-2 ms-2">
      <Form.Label>or upload a <i>custom</i> scenario:</Form.Label>
      <Form.Control type="file" 
                    onChange={(event) => {
                        if (event.target.files) {
                          let file = event.target.files[0];
                          onCustomScenarioFileSelected(file);
                        }
                      }
                    }/>

    </Form.Group>
    <b className="mt-3">Scenario:</b>
    <br></br>
    <label className="mt-1 ms-1 me-2">{currentScenarioName ? currentScenarioName : "not selected"}</label>
    <Badge bg="secondary">{currentScenarioType || ""}</Badge>
    </>
  );
}

export default ScenarioList;
