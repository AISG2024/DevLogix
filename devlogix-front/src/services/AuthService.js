export const API_URL = "https://enfycius.com:8008/api";

export const getAccessToken = () => localStorage.getItem("accessToken");
export const getRefreshToken = () => localStorage.getItem("refreshToken");

export const registerUser = async (username, password) => {
  const response = await fetch(`${API_URL}/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    const errorMessage = await response.text(); 
    throw new Error(errorMessage);
  }

  return await response.text();
};

export const loginUser = async (username, password) => {
  const response = await fetch(`${API_URL}/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error("Login failed");
  }

  const data = await response.json();

  if (!data.refreshToken || !data.accessToken) {
    console.error("Missing tokens in response:", data);
    throw new Error("Login response is missing tokens");
  }

  localStorage.setItem("accessToken", data.accessToken);
  localStorage.setItem("refreshToken", data.refreshToken);
  localStorage.setItem("username", username);

  return data;
};

export const logoutUser = async () => {
  const refreshToken = localStorage.getItem("refreshToken");

  const response = await fetch(`${API_URL}/logout`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${refreshToken}`,
      "Content-Type": "application/json",
    },
  });

  console.log("Server Response:", await response.text());

  if (!response.ok) {
    console.error("Logout failed with status:", response.status);
    throw new Error("Logout failed");
  }

  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("username");

  return "Logged out successfully";
};

export const refreshToken = async () => {
  const refreshToken = localStorage.getItem("refreshToken");

  const response = await fetch(`${API_URL}/refresh-token`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${refreshToken}`,
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error("Failed to refresh token");
  }

  const data = await response.json();
  
  localStorage.setItem("accessToken", data.accessToken);

  return data.accessToken;
};

export const authenticatedRequest = async (endpoint, method = "GET") => {
  let accessToken = localStorage.getItem("accessToken");

  const response = await fetch(`${API_URL}${endpoint}`, {
    method,
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
  });

  if (response.status === 401) {
    accessToken = await refreshToken();
    return authenticatedRequest(endpoint, method);
  }

  if (!response.ok) {
    throw new Error("Request failed");
  }

  return await response.json();
};

export const decodeToken = (token) => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload;
  } catch (e) {
    console.error("Invalid token", e);
    return null;
  }
};