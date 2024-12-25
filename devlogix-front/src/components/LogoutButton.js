import React, { useState } from "react";
import { logoutUser } from "../services/AuthService";

const LogoutButton = ({ onLogout, isLoggingOut }) => {
  const handleLogout = async () => {
    if (isLoggingOut) return;
    try {
      await onLogout();
    } catch (error) {
      alert(error.message);
    }
  };

  return (
    <button
      onClick={handleLogout}
      className="btn btn-danger"
      disabled={isLoggingOut}
    >
      {isLoggingOut ? "Logging Out..." : "Logout"}
    </button>
  );
};

export default LogoutButton;