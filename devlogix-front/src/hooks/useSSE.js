import { useEffect, useState, useRef } from "react";
import { getAccessToken, refreshToken, API_URL } from "../services/AuthService";

const sharedSSE = {
  eventSources: {},
  listeners: {},    

  connect: async (endpoint, serviceType) => {
    if (sharedSSE.eventSources[serviceType]) {
      // console.log(`SSE connection for ${serviceType} already exists`);
      return;
    }

    const accessToken = getAccessToken();
    const username = localStorage.getItem("username");

    if (!accessToken || !username) {
      // console.error("Access token or username is missing. Please log in.");
      throw new Error("Access token or username is missing. Please log in.");
    }

    const eventSource = new EventSource(
      `${API_URL}${endpoint}?token=${accessToken}&username=${username}&serviceType=${serviceType}`
    );

    sharedSSE.eventSources[serviceType] = eventSource;

    if (!sharedSSE.listeners[serviceType]) {
      sharedSSE.listeners[serviceType] = new Set();
    }

    eventSource.onopen = () => {
      // console.log(`SSE connection opened for: ${serviceType}`);
    };

    eventSource.addEventListener("updateEvent", (event) => {
      // console.log(`SSE updateEvent received for ${serviceType}:`, event.data);
      sharedSSE.listeners[serviceType].forEach((listener) => listener(event.data));
    });

    eventSource.onerror = async (error) => {
      // console.error(`SSE connection error for ${serviceType}, reconnecting...`, error);
      eventSource.close();
      delete sharedSSE.eventSources[serviceType];

      const refreshedToken = await refreshToken();
      if (refreshedToken) {
        // console.log(`Token refreshed. Reconnecting SSE for ${serviceType}...`);
        sharedSSE.connect(endpoint, serviceType);
      } else {
        // console.error(`Token refresh failed. SSE disconnected for ${serviceType}.`);
      }
    };
  },

  addListener: (serviceType, listener) => {
    if (!sharedSSE.listeners[serviceType]) {
      sharedSSE.listeners[serviceType] = new Set();
    }
    sharedSSE.listeners[serviceType].add(listener);
  },

  removeListener: (serviceType, listener) => {
    if (sharedSSE.listeners[serviceType]) {
      sharedSSE.listeners[serviceType].delete(listener);
      if (sharedSSE.listeners[serviceType].size === 0) {
        sharedSSE.disconnect(serviceType);
      }
    }
  },

  disconnect: (serviceType) => {
    if (sharedSSE.eventSources[serviceType]) {
      // console.log(`Closing SSE connection for ${serviceType}`);
      sharedSSE.eventSources[serviceType].close();
      delete sharedSSE.eventSources[serviceType];
      delete sharedSSE.listeners[serviceType];
    }
  },
};

const useSSE = (endpoint, serviceType) => {
  const [events, setEvents] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const handleEvent = (data) => {
      // console.log(`Handling SSE event for ${serviceType}:`, data);
      setEvents((prev) => [...prev, data]);
    };

    const connect = async () => {
      try {
        // console.log(`Connecting SSE for: ${serviceType}`);
        await sharedSSE.connect(endpoint, serviceType);
        sharedSSE.addListener(serviceType, handleEvent); // Add listener
      } catch (err) {
        // console.error(`SSE connection error for ${serviceType}:`, err);
        // setError(err.message);
      }
    };

    connect();

    return () => {
      // console.log(`Cleaning up SSE listeners for ${serviceType}...`);
      sharedSSE.removeListener(serviceType, handleEvent); // Remove listener
    };
  }, [endpoint, serviceType]);

  return { events, error };
};

export default useSSE;