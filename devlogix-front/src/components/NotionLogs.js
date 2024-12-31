import React, { useEffect, useState } from "react";
import useSSE from "../hooks/useSSE";
import { getAllNotion } from "../services/NotionService";

const NotionLogs = () => {
  const { events, error: sseError } = useSSE("/notion/events", "notion");
  const [logs, setLogs] = useState([]);
  const [fetchError, setFetchError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const data = await getAllNotion();
        const sortedData = data.sort(
          (a, b) => new Date(b.lastEditedTime) - new Date(a.lastEditedTime)
        );
        setLogs(sortedData);
      } catch (err) {
        console.error("Error fetching Notion logs:", err);
        setFetchError(err.message || "An error occurred while fetching logs.");
      } finally {
        setLoading(false);
      }
    };

    fetchLogs();
  }, []);

  useEffect(() => {
    if (events.length > 0) {
      const fetchUpdatedLogs = async () => {
        try {
          const updatedData = await getAllNotion();
          const sortedData = updatedData.sort(
            (a, b) => new Date(b.lastEditedTime) - new Date(a.lastEditedTime)
          );
          setLogs(sortedData);
        } catch (err) {
          console.error("Error fetching updated logs:", err);
          setFetchError(err.message || "An error occurred while fetching updated logs.");
        }
      };

      fetchUpdatedLogs();
    }
  }, [events]);

  if (sseError) return <p>Error with SSE: {sseError}</p>;
  if (fetchError) return <p>Error fetching logs: {fetchError}</p>;

  return (
    <div>
      <h2>Notion Logs</h2>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>#</th>
            <th>Name</th>
            <th>Field</th>
            <th>Person</th>
            <th>Page ID</th>
            <th>Received At</th>
            <th>Last Edited Time</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan="6" style={{ textAlign: "center" }}>
                Loading...
              </td>
            </tr>
          ) : logs.length === 0 ? (
            <tr>
              <td colSpan="6" style={{ textAlign: "center" }}>
                No logs available
              </td>
            </tr>
          ) : (
            logs.map((log, index) => (
              <tr key={`${log.pageId}-${index}`}>
                <td>{index + 1}</td>
                <td>{log.name}</td>
                <td>{log.fieldNames}</td>
                <td>{log.personNames}</td>
                <td>{log.pageId}</td>
                <td>{new Date(log.receivedAt).toLocaleString()}</td>
                <td>{new Date(log.lastEditedTime).toLocaleString()}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default NotionLogs;