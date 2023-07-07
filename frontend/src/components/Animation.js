import React, { Suspense, Fragment } from "react";
import { Canvas } from "@react-three/fiber";
import { OrbitControls, Plane, Line, Html } from "@react-three/drei";


const Truck = ({truck}) => {
    return (
        <group position={[truck.x, -0.5, truck.y]} rotation-y={truck.heading + Math.PI}>
            <mesh key={"truckhead-" + truck.id} position={[0, 0, 0]}>
                <boxGeometry args={[0.8, 0.7, 0.4]} />
                <meshStandardMaterial color={"green"} />
            </mesh>
            <mesh key={"truckbody-" + truck.id} position={[0, 0, 1.7]}>
                <boxGeometry args={[1, 1, 3]} />
                <meshStandardMaterial color={truck.withCargo ? "royalblue" : "gray"} />
            </mesh>
            <Html center position={[2, 0, 0]}>
              <div style={{ background: truck.withCargo ? "rgba(180,200,250,0.5)" : "rgba(220,220,220,0.5)" , whiteSpace: "nowrap"}}>{truck.name}</div>
            </Html>
        </group>
    )
}


const Store = ({store, nodes}) => {
    return (
        <group key={"store-" + store.name} position={[nodes.get(store.node).x, -0.75, nodes.get(store.node).y]}>
            <mesh   scale={[2, 1, 2]}>
                    <boxGeometry args={[2, 2, 2]} />
                    <meshStandardMaterial color={"red"} />
            </mesh>
            <Html center position={[0, 0, 2.7]}>
              <div style={{ background: "rgba(240,200,200,0.5)", whiteSpace: "nowrap"}}>{store.name}</div>
            </Html>
        </group>
    )
}

const Warehouse = ({warehouse, nodes}) => {
    return (
        <group key={"warehouse-" + warehouse.name} position={[nodes.get(warehouse.node).x, -0.75, nodes.get(warehouse.node).y]}>
            <mesh   scale={[2, 1, 2]}>
                    <boxGeometry args={[2, 2, 2]} />
                    <meshStandardMaterial color={"orange"} />
            </mesh>
            <Html center position={[0, 0, 2.7]}>
              <div style={{ background: "rgba(240,240,190,0.5)", whiteSpace: "nowrap"}}>{warehouse.name}</div>
            </Html>
        </group>
    )
}

const MainScene = ({arcs, nodes, stores, warehouses, trucks}) => {
    return (
        <>
            {arcs.map((arc) => (
                <Line
                    key={"arc-" + arc.source + "-" + arc.dest}
                    points={[
                    [nodes.get(arc.source).x, -0.6, nodes.get(arc.source).y],
                    ...arc.points.map((point) => [point.x, -0.6, point.y]),
                    [nodes.get(arc.dest).x, -0.6, nodes.get(arc.dest).y],
                    ]} />
            ))}
            {stores.map((store) => (
                <Store store={store} nodes={nodes}/>
            ))}
            {warehouses.map((warehouse) => ( 
                <Warehouse warehouse={warehouse} nodes={nodes} />
            ))}
            {trucks.map((truck) => (
                <Truck truck={truck} />
            ))}

            <Plane
                    scale={[2000, 2000, 0]}
                    position={[0, -0.75, 0]}
                    rotation-x={Math.PI * -0.5}
                />

            {/* scene setup */}
            <ambientLight intensity={0.1} />
            <directionalLight intensity={1} color="white" />
            <OrbitControls makeDefault />
        </>
    )
}


function Animation({arcs, nodes, stores, warehouses, trucks}) {
    return (
        <Suspense fallback={null}>
            <Canvas camera={{
                position: [0, 400, 0],
                fov: 45,
                far: 2000,
            }}>
                <MainScene arcs={arcs} nodes={nodes} stores={stores} warehouses={warehouses} trucks={trucks} />
            </Canvas>
        </Suspense>
    );
}

export default Animation;

