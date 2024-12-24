import React, { useState, useEffect } from "react";
import { authenticatedRequest } from "../services/AuthService";

const ProtectedContent = () => {
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await authenticatedRequest("/test");
        setMessage(data.message);
      } catch (error) {
        setMessage(error.message);
      }
    };
    fetchData();
  }, []);

  return (
    <div className="card p-4 shadow-sm">
      <h2 className="text-center">Protected Content</h2>
      <p>{message}</p>
    </div>
  );
};

export default ProtectedContent;