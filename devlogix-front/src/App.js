import React, { useState } from "react";
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from "react-router-dom";
import LoginForm from "./components/LoginForm";
import LogoutButton from "./components/LogoutButton";
import RegisterForm from "./components/RegisterForm";
import Home from "./components/Home";
import CommitLog from "./components/CommitLogs";

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const handleLoginSuccess = () => {
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
  };

  return (
    <Router>
      <div className="container mt-5">
        <h1 className="text-center mb-4">JWT Auth App</h1>
        <div className="d-flex justify-content-between align-items-start mb-4">
          <div className="d-flex flex-column align-items-start">
            <Link to="/" className="btn btn-light mb-2">
              Home
            </Link>
            {isLoggedIn && (
              <>
                <Link to="/statistics" className="btn btn-light mb-2">
                  Commit Statistics
                </Link>
                <Link to="/logs" className="btn btn-light mb-2">
                  Commit Log
                </Link>
              </>
            )}
          </div>
          <div>
            {!isLoggedIn && <Link to="/login" className="btn btn-primary mx-2">Login</Link>}
            {!isLoggedIn && <Link to="/register" className="btn btn-secondary mx-2">Register</Link>}
            {isLoggedIn && <LogoutButton onLogout={handleLogout} />}
          </div>
        </div>
        <Routes>
          <Route
            path="/login"
            element={isLoggedIn ? <Navigate to="/" /> : <LoginForm onLoginSuccess={handleLoginSuccess} />}
          />
          <Route
            path="/register"
            element={isLoggedIn ? <Navigate to="/" /> : <RegisterForm />}
          />
          <Route
            path="/statistics"
            element={isLoggedIn ? <Home /> : <Navigate to="/login" />}
          />
          <Route
            path="/logs"
            element={isLoggedIn ? <CommitLog /> : <Navigate to="/login" />}
          />
          <Route
            path="/"
            element={isLoggedIn ? <Home /> : <Navigate to="/login" />}
          />
        </Routes>
      </div>
    </Router>
  );
}

export default App;