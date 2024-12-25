import React from "react";
import Graph from "./Graph";

const Home = () => {
  return (
    <div className="container mt-5">
      <h1 className="text-center">Commit Statistics</h1>
      <Graph />
    </div>
  );
};

export default Home;