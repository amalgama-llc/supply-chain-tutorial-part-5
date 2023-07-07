import React from "react";

function SummaryStatistics(props) {
    return (
      <>
        <h5 className="mt-1">Summary statistics</h5>
        <table>
          <thead>
            <tr>
              <th>Indicator name</th>
              <th>Value</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Service level</td>
              <td>{props.stats.serviceLevelPct ? props.stats.serviceLevelPct : 0} %</td>
            </tr>
            <tr>
              <td>Expenses</td>
              <td>
                $ {props.numberWithSpaces((props.stats.expenses||0).toFixed(2))}
              </td>
            </tr>
          </tbody>
        </table>
      </>
    );
}

export default SummaryStatistics;
