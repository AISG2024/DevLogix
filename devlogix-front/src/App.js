import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import RegisterForm from "./components/RegisterForm";
import LoginForm from "./components/LoginForm";
import ProtectedContent from "./components/ProtectedContent";

function App() {
  return (
    <Router>
      <div className="container mt-5">
        <h1 className="text-center mb-4">JWT Auth App</h1>
        <div className="d-flex justify-content-center mb-4">
          <Link to="/register" className="btn btn-primary mx-2">Register</Link>
          <Link to="/login" className="btn btn-secondary mx-2">Login</Link>
          <Link to="/protected" className="btn btn-success mx-2">Protected Content</Link>
        </div>
        <Routes>
          <Route path="/register" element={<RegisterForm />} />
          <Route path="/login" element={<LoginForm />} />
          <Route path="/protected" element={<ProtectedContent />} />
          <Route path="/" element={<h2 className="text-center">Welcome to JWT Auth App!</h2>} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;