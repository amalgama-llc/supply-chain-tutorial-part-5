import React from "react";
import { Chart } from "react-google-charts";

function GanttChart({gantts}) {

  const columns = [
      { type: "string", id: "Truck" },
      { type: "string", id: "Name" },
      { type: "datetime", id: "Start" },
      { type: "datetime", id: "End" },
  ];
    
  const data = [columns, ...gantts];
  const options = {
    enableInteractivity: false,
    tooltip: { trigger: "none" },
    width: "100%",
    height: 400,
    groupByRowLabel: false,
    timeline: {
      colorByRowLabel: true,
      singleColor: "0065ad",
    },
  };

  return (
        <>
        {gantts.length === 0 
          ? "Will be displayed when some transportation task is finished." 
          : (<Chart chartType="Timeline" data={data} options={options} />)}
        </>
  );
}

export default GanttChart;