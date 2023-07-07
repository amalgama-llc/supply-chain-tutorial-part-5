import React from "react";
import { numberWithSpaces } from "../utils";

function TruckStatistics({trucks}) {
    return (
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Expenses</th>
                <th>Distance traveled, km</th>
            </tr>
            </thead>
            {trucks && (
            <tbody>
                {trucks.map((truck) => (
                <tr key={truck.id}>
                    <td>{truck.id}</td>
                    <td>{truck.name}</td>
                    <td>$ {numberWithSpaces(truck.expenses.toFixed(2))}</td>
                    <td>{truck.distanceTraveled.toFixed(2)}</td>
                </tr>
                ))}
            </tbody>
            )}
        </table>
    );
}

export default TruckStatistics;
