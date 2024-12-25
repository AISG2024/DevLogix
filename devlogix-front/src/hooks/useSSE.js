import { useEffect, useState, useRef } from "react";
import { getAccessToken, refreshToken, API_URL } from "../services/AuthService";

const sharedSSE = {
  eventSource: null,
  listeners: new Set(),
  connect: async (endpoint) => {
    if (sharedSSE.eventSource) {
      return;
    }

    const accessToken = getAccessToken();
    const username = localStorage.getItem("username");

    if (!accessToken || !username) {
      throw new Error("Access token or username is missing. Please log in.");
    }

    const eventSource = new EventSource(
      `${API_URL}${endpoint}?token=${accessToken}&username=${username}`
    );

    sharedSSE.eventSource = eventSource;

    eventSource.onopen = () => {
    };

    eventSource.addEventListener("updateEvent", (event) => {
      sharedSSE.listeners.forEach((listener) => listener(event.data));
    });

    eventSource.onerror = async () => {
      eventSource.close();
      sharedSSE.eventSource = null;

      const refreshedToken = await refreshToken();
      if (refreshedToken) {
        sharedSSE.connect(endpoint);
      } else {
      }
    };
  },
  disconnect: () => {
    if (sharedSSE.eventSource) {
      sharedSSE.eventSource.close();
      sharedSSE.eventSource = null;
    }
  },
};

const useSSE = (endpoint) => {
  const [events, setEvents] = useState([]);
  const [error, setError] = useState("");
  const eventSourceRef = useRef(null);

  useEffect(() => {
    const handleEvent = (data) => {
      setEvents((prev) => [...prev, data]);
    };

    const connect = async () => {
      try {
        await sharedSSE.connect(endpoint);
        sharedSSE.listeners.add(handleEvent);
      } catch (err) {
      }
    };

    connect();

    return () => {
      sharedSSE.listeners.delete(handleEvent);
      if (sharedSSE.listeners.size === 0) {
        sharedSSE.disconnect();
      }
    };
  }, [endpoint]);

  return { events, error };
};

export default useSSE;