import { authenticatedRequest } from "./AuthService";

export const getTodayCommits = async () => {
  try {
    const data = await authenticatedRequest("/commits/today", "GET");
    return data; 
  } catch (error) {
    console.error("Error fetching today's commits:", error.message);
    throw error; 
  }
};