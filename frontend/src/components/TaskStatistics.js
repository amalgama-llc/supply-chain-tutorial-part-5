import React from "react";
import { formatDateTime } from "../utils";

function TaskStatistics({tasks}) {
    return (
        <table>
            <thead>
            <tr>
                <th>Id</th>
                <th>Truck</th>
                <th>Source</th>
                <th>Destination</th>
                <th>Created</th>
                <th>Started</th>
                <th>Deadline</th>
                <th>Completed</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            {tasks &&
                tasks.map((task) => (
                <tr key={task.id}>
                    <td>{task.id}</td>
                    <td>{task.truck}</td>
                    <td>{task.source}</td>
                    <td>{task.destination}</td>
                    <td>{formatDateTime(task.created)}</td>
                    <td>{formatDateTime(task.started)}</td>
                    <td>{formatDateTime(task.deadline)}</td>
                    <td>{formatDateTime(task.completed)}</td>
                    <td>{task.status.replaceAll('_', ' ')}</td>
                </tr>
                ))}
            </tbody>
        </table>
    );
}

export default TaskStatistics;
