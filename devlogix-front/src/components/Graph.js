import React, { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import "chart.js/auto";
import { getTodayCommits } from "../services/CommitService";
import useSSE from "../hooks/useSSE";
import "./Graph.css";

const Graph = () => {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const { events, error: sseError } = useSSE("/mattermost/events");

  const fetchData = async () => {
    try {
      const result = await getTodayCommits();
      const labels = Object.keys(result);
      const values = Object.values(result);

      const updatedData = {
        labels: [...labels],
        datasets: [
          {
            label: "Commits Today",
            data: [...values],
            backgroundColor: "rgba(75, 192, 192, 0.6)",
          },
        ],
      };

      setData(updatedData);
    } catch (err) {
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (events.length > 0) {
      fetchData();
    }
  }, [events]);

  if (sseError) return <p>Error with SSE: {sseError}</p>;
  if (error) return <p>Error: {error}</p>;
  if (!data) return <p>Loading...</p>;

  return (
    <div className="chart-container">
      <Bar
        data={data}
        options={{
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: "top" },
            title: { display: true, text: "Today's Commits by User" },
          },
        }}
      />
    </div>
  );
};

export default Graph;