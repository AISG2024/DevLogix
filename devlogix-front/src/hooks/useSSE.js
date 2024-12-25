import { useEffect, useState } from "react";
import { getAccessToken, refreshToken, API_URL } from "../services/AuthService";

const sharedSSEFetch = {
  abortController: null,
  listeners: new Set(),
  connect: async (endpoint) => {
    if (sharedSSEFetch.abortController) {
      return;
    }

    try {
      const accessToken = getAccessToken();
      const username = localStorage.getItem("username");

      if (!accessToken || !username) {
        throw new Error("Access token or username is missing. Please log in.");
      }

      sharedSSEFetch.abortController = new AbortController();
      const { signal } = sharedSSEFetch.abortController;

      const response = await fetch(`${API_URL}${endpoint}?token=${accessToken}&username=${username}`, {
        headers: {
          Accept: "text/event-stream",
          "Cache-Control": "no-cache", 
          "Connection": "keep-alive",
        },
        signal,
      });

      if (!response.ok) {
        throw new Error(`Failed to connect to SSE: ${response.statusText}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder("utf-8");

      while (true) {
        try {
          const { value, done } = await reader.read();
          if (done) break;
      
          const chunk = decoder.decode(value, { stream: true });
      
          const lines = chunk.split("\n").filter((line) => line.trim() !== "");
      
          lines.forEach((line) => {
            if (line.startsWith("data:")) {
              const rawData = line.replace("data:", "").trim();
      
              try {
                const parsedData = rawData.startsWith("{") && rawData.endsWith("}")
                  ? JSON.parse(rawData)
                  : rawData;
      
                if (typeof parsedData === "object") {
                  sharedSSEFetch.listeners.forEach((listener) => listener(parsedData));
                } else if (typeof parsedData === "string") {
                  console.log("Received string data:", parsedData);
                  sharedSSEFetch.listeners.forEach((listener) => listener(parsedData));
                } else {
                  console.warn("Received unknown SSE data type:", rawData);
                }
              } catch (parseError) {
                console.error("Invalid JSON in SSE data:", rawData, parseError);
              }
            } else if (line.startsWith("event:")) {
              console.log("Received SSE event type:", line.replace("event:", "").trim());
            } else {
              console.warn("Unrecognized SSE line:", line);
            }
          });
        } catch (readError) {
          break;
        }
      }
    } catch (error) {
      if (error.name !== "AbortError") {
        try {
          const refreshedToken = await refreshToken();
          if (refreshedToken) {
            await sharedSSEFetch.connect(endpoint);
          }
        } catch (refreshError) {
          console.error("Failed to refresh token:", refreshError);
        }
      }
    } finally {
      sharedSSEFetch.abortController = null;
    }
  },

  disconnect: () => {
    if (sharedSSEFetch.abortController) {
      sharedSSEFetch.abortController.abort();
      sharedSSEFetch.abortController = null;
    }
  },
};

const useSSEFetch = (endpoint) => {
  const [events, setEvents] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const handleEvent = (data) => {
      setEvents((prev) => [...prev, data]);
    };

    const connect = async () => {
      try {
        await sharedSSEFetch.connect(endpoint);
        sharedSSEFetch.listeners.add(handleEvent);
      } catch (err) {
        setError("Error connecting to SSE.");
      }
    };

    connect();

    return () => {
      sharedSSEFetch.listeners.delete(handleEvent);
      if (sharedSSEFetch.listeners.size === 0) {
        sharedSSEFetch.disconnect();
      }
    };
  }, [endpoint]);

  return { events, error };
};

export default useSSEFetch;