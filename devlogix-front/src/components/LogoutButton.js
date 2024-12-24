import React from "react";
import { logoutUser } from "../services/AuthService";

const LogoutButton = ({ onLogout }) => {
  const handleLogout = async () => {
    try {
      const message = await logoutUser();
      alert(message);
      onLogout(); 
    } catch (error) {
      alert(error.message);
    }
  };

  return (
    <button onClick={handleLogout} className="btn btn-danger">
      Logout
    </button>
  );
};

export default LogoutButton;