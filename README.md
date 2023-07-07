# Part 5: Supply Chain Web Application

## About
This repository contains the source code for the [Part 5 of the Supply Chain tutorial](https://platform.amalgamasimulation.com/amalgama/SupplyChainTutorial/part5/sc_tutorial_part_5.html).

The web application uses the supply chain simulation logic implemented in
[Part 3 of the Supply Chain tutorial](https://platform.amalgamasimulation.com/amalgama/SupplyChainTutorial/part3/sc_tutorial_part_3.html)
to model a delivery network of warehouses and stores.

It consists of a backend application that runs the simulation and a frontend application 
that allows to load a simulation scenario, control the simulation flow, and see the current simulation status.

## Backend: how to build and run locally
1. Download and install JDK-17 and Maven 3.8.6+.
2. Download the source code of the desktop application for Part 3 of the Supply Chain tutorial: https://github.com/amalgama-llc/supply-chain-tutorial-part-3.
3. In the Part 3 source code folder, run `mvn clean package` console command . After the compilation is finished, a new folder `releng/com.company.tutorial3.product/target/repository` will be created. Its `plugins` subfolder contains the jar files with the simulation logic that we need: 
   - `com.company.tutorial3.datamodel_1.0.0.jar` and 
   - `com.company.tutorial3.simulation_1.0.0.jar`.
4. In that `plugins` subfolder, run the following two commands to install the simulation logic libraries into the local Maven repository:

```
mvn install:install-file -Dfile=com.company.tutorial3.datamodel_1.0.0.jar -DgroupId=com.company.tutorial3 -DartifactId=datamodel -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=com.company.tutorial3.simulation_1.0.0.jar -DgroupId=com.company.tutorial3 -DartifactId=simulation -Dversion=1.0.0 -Dpackaging=jar -DgeneratePom=true
```

5. In the Part 5 source code folder, go to the `backend` folder and run `mvn spring-boot:run` console command.

After the web service is started, open this link in your browser:

http://localhost:8080/hello

This should show 'Hello, World', meaning that the backend has started successfully.

## Frontend: how to build and run locally
1. Go to the [Node.js site](https://nodejs.org/en) and download the Node.js version marked as 'Recommended for Most Users'. Install Node.js.
2. In the `frontend` folder, run `npm install` to download and install the required dependencies.
3. Make sure the backend application is running.
4. In the `frontend` folder, run `npm start` to start the frontend application. 
A browser window should open at the URL http://localhost:3000 and show the web application GUI.

## How to deploy the web application to a server
See https://platform.amalgamasimulation.com/amalgama/SupplyChainTutorial/part5/sc_tutorial_step_5.6.html.

## How to use

In the browser window with the web application, select an embedded scenario from the list in the top left part.

Click 'START' to start the simulation.
You can change the speed of the simulation, make it faster or slower.

Clicking 'STOP' does not reset the simulation; you can 'RESUME' it later.

When you 'RESET' the simulation, the modeling information is cleared, the model time is set to the start. You can then 'START' the simulation again.

The 'Animation' window (top right) shows the road network, stores, warehouses, and trucks travelling among them.

The bottom right part of the window shows truck statistics, the Gantt chart of completed tasks, and the list of all transportation tasks.

The bottom left part shows the overall simulation statistics (service level and total expences). Service level is 100% when all tasks are completed in time (before deadline).

**To run a simulation with a custom Excel scenario**, download the scenario from https://github.com/amalgama-llc/supply-chain-tutorial-part-3/tree/main/scenario
and upload it in the upper left pane of the application window.

