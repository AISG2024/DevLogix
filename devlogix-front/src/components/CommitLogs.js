import React, { useEffect, useState } from "react";
import { getAllCommits } from "../services/CommitService";

const CommitLogs = () => {
  const [logs, setLogs] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchLogs = async () => {
      try {
        const data = await getAllCommits();
        setLogs(data);
      } catch (err) {
        setError(err.message);
      }
    };

    fetchLogs();
  }, []);

  if (error) return <p>Error: {error}</p>;
  if (!logs.length) return <p>Loading...</p>;

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
          {logs.map((log, index) => (
            <tr key={log.id}>
              <td>{index + 1}</td>
              <td>{log.repositoryName}</td>
              <td>{log.commitId}</td>
              <td>{log.commitMessage}</td>
              <td>{log.userName}</td>
              <td>{log.channelName}</td>
              <td>{new Date(log.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default CommitLogs;