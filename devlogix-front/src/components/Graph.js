import React, { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import "chart.js/auto";
import { getTodayCommits } from "../services/CommitService";
import useSSE from "../hooks/useSSE";
import "./Graph.css";

const Graph = () => {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const { events, error: sseError } = useSSE("/mattermost/events", "mattermost");

  const generateColors = (count) => {
    const colors = [];
    for (let i = 0; i < count; i++) {
      const r = Math.floor(Math.random() * 256);
      const g = Math.floor(Math.random() * 256);
      const b = Math.floor(Math.random() * 256);
      colors.push(`rgba(${r}, ${g}, ${b}, 0.6)`);
    }
    return colors;
  };

  const fetchData = async () => {
    try {
      const result = await getTodayCommits();

      if (!result || Object.keys(result).length === 0) {
        setData({
          labels: ["No Data"],
          datasets: [
            {
              label: "Commits Today",
              data: [0],
              backgroundColor: ["rgba(200, 200, 200, 0.6)"],
            },
          ],
        });
        return;
      }

      const labels = Object.keys(result);
      const values = Object.values(result);

      const combined = labels.map((label, index) => ({
        label,
        value: values[index],
      }));

      const sorted = combined.sort((a, b) => b.value - a.value);

      const sortedLabels = sorted.map((item) => item.label);
      const sortedValues = sorted.map((item) => item.value);

      const colors = generateColors(sortedValues.length);

      const updatedData = {
        labels: [...sortedLabels],
        datasets: [
          {
            label: "Commits Today",
            data: [...sortedValues],
            backgroundColor: colors,
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