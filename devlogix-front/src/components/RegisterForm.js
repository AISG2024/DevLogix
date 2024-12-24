import React, { useState } from "react";
import { registerUser } from "../services/AuthService";

const RegisterForm = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [usernameError, setUsernameError] = useState(false);
  const [passwordError, setPasswordError] = useState(false); 
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    let hasError = false;

    if (!username.trim()) {
      setUsernameError(true);
      hasError = true;
    } else {
      setUsernameError(false);
    }

    if (!password.trim()) {
      setPasswordError(true);
      hasError = true;
    } else {
      setPasswordError(false);
    }

    if (hasError) {
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await registerUser(username, password);
      setMessage(response);
    } catch (error) {
      setMessage(error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="card p-4 shadow-sm">
      <h2 className="text-center">Register</h2>
      <form onSubmit={handleSubmit}>
        <div className="form-group mb-3">
          <label>Username:</label>
          <input
            type="text"
            className={`form-control ${usernameError ? "is-invalid" : ""}`}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          {usernameError && (
            <div className="invalid-feedback">Username is required.</div>
          )}
        </div>
        <div className="form-group mb-3">
          <label>Password:</label>
          <input
            type="password"
            className={`form-control ${passwordError ? "is-invalid" : ""}`}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          {passwordError && (
            <div className="invalid-feedback">Password is required.</div>
          )}
        </div>
        <button type="submit" className="btn btn-primary w-100" disabled={isSubmitting}>
          {isSubmitting ? "Registering..." : "Register"}
        </button>
      </form>
      {message && (
        <div
          className={`alert mt-3 ${
            message.includes("successfully") ? "alert-success" : "alert-danger"
          }`}
        >
          {message}
        </div>
      )}
    </div>
  );
};

export default RegisterForm;