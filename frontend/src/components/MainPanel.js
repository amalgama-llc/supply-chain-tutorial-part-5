import React from "react";
import { Tab, Row, Col, Nav } from "react-bootstrap";
import Split from 'react-split'
import TaskStatistics from "./TaskStatistics";
import TruckStatistics from "./TruckStatistics";
import Animation from "./Animation";
import GanttChart from "./GanttChart";

function MainPanel({arcs, nodes, warehouses, stores, trucks, tasks, gantts}) {
  return (
    <Split 
      direction="vertical"
      style={{ height: "96vh"}}>
        <div className="animation-panel">
            <Animation  arcs={arcs}
                        nodes={nodes}
                        warehouses={warehouses}
                        stores={stores}
                        trucks={trucks}/>
        </div>
        <div>
        <Tab.Container id="top-tabs" defaultActiveKey="trucks">
          <Row className="gx-0" style={{height: "100%"}}>
            <Col style={{height: "100%"}}>
              <Nav variant="underline" className="flex-row">
                <Nav.Item>
                  <Nav.Link eventKey="trucks">Trucks</Nav.Link>
                </Nav.Item>
                <Nav.Item>
                  <Nav.Link eventKey="gantt">Gantt chart</Nav.Link>
                </Nav.Item>
                <Nav.Item>
                  <Nav.Link eventKey="tasks">Tasks</Nav.Link>
                </Nav.Item>
              </Nav>
              <Tab.Content className="m-1" style={{ overflowY: "scroll", height: `calc(100% - 2rem)`}}>
                <Tab.Pane eventKey="trucks">
                  <TruckStatistics trucks={trucks} /> 
                </Tab.Pane>
                <Tab.Pane eventKey="gantt">
                  <GanttChart gantts={gantts} /> 
                </Tab.Pane>
                <Tab.Pane eventKey="tasks">
                  <TaskStatistics tasks={tasks} />
                </Tab.Pane>
              </Tab.Content>
            </Col>
          </Row>
        </Tab.Container>
        </div>
    </Split>
  );
}

export default MainPanel;
