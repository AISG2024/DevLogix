import React, { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import "chart.js/auto";
import { getTodayCommits } from "../services/CommitService";
import "./Graph.css";

const Graph = () => {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await getTodayCommits();
        const labels = Object.keys(result);
        const values = Object.values(result);

        setData({
          labels,
          datasets: [
            {
              label: "Commits Today",
              data: values,
              backgroundColor: "rgba(75, 192, 192, 0.6)",
            },
          ],
        });
      } catch (err) {
        setError(err.message);
      }
    };

    fetchData();
  }, []);

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