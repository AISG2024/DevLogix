import React, { useEffect, useState } from "react";
import useSSE from "../hooks/useSSE";
import { getAllCommits } from "../services/CommitService";

const CommitLogs = () => {
  const { events, error: sseError } = useSSE("/mattermost/events", "mattermost");
  const [logs, setLogs] = useState([]);
  const [fetchError, setFetchError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const data = await getAllCommits();
        const sortedData = data.sort(
          (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
        );
        setLogs(sortedData);
      } catch (err) {
        console.error("Error fetching commits:", err);
        setFetchError(err.message || "An error occurred while fetching commits.");
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
          const updatedData = await getAllCommits();
          const sortedData = updatedData.sort(
            (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
          );
          setLogs(sortedData);
        } catch (err) {
          console.error("Error fetching updated commits:", err);
        }
      };

      fetchUpdatedLogs();
    }
  }, [events]);

  if (sseError) return <p>Error with SSE: {sseError}</p>;
  if (fetchError) return <p>Error fetching commits: {fetchError}</p>;

  return (
    <div>
      <h2>Commit Logs</h2>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>#</th>
            <th>Repository</th>
            <th>Commit ID</th>
            <th>Commit Message</th>
            <th>Author</th>
            <th>Channel</th>
            <th>Timestamp</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan="7" style={{ textAlign: "center" }}>Loading...</td>
            </tr>
          ) : logs.length === 0 ? (
            <tr>
              <td colSpan="7" style={{ textAlign: "center" }}>No commits available</td>
            </tr>
          ) : (
            logs.map((log, index) => (
              <tr key={`${log.id}-${index}`}>
                <td>{index + 1}</td>
                <td>{log.repositoryName}</td>
                <td>{log.commitId}</td>
                <td>{log.commitMessage}</td>
                <td>{log.userName}</td>
                <td>{log.channelName}</td>
                <td>{new Date(log.createdAt).toLocaleString()}</td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

export default CommitLogs;