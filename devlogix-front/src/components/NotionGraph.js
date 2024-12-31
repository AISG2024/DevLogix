import React, { useEffect, useState } from "react";
import { Bar } from "react-chartjs-2";
import "chart.js/auto";
import { getTodayNotion } from "../services/NotionService";
import useSSE from "../hooks/useSSE";
import "./Graph.css";

const NotionGraph = () => {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const { events, error: sseError } = useSSE("/notion/events", "notion");

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
      const notionStats = await getTodayNotion();

      if (!notionStats || Object.keys(notionStats).length === 0) {
        setData({
          labels: ["No Data"],
          datasets: [
            {
              label: "Notion Data",
              data: [0],
              backgroundColor: ["rgba(200, 200, 200, 0.6)"],
            },
          ],
        });
        return;
      }

      const labels = Object.keys(notionStats);
      const values = Object.values(notionStats);

      const colors = generateColors(values.length);

      setData({
        labels,
        datasets: [
          {
            label: "Entries per User",
            data: values,
            backgroundColor: colors,
          },
        ],
      });
    } catch (err) {
      // console.error("Error fetching Notion data:", err);
      // setError(err.message);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (events.length > 0) {
      // console.log("SSE events received, updating data...");
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
            title: { display: true, text: "Notion Entries by User" },
          },
        }}
      />
    </div>
  );
};

export default NotionGraph;