import { authenticatedRequest } from "./AuthService";

export const getAllCommits = async () => {
  try {
    const data = await authenticatedRequest("/commits/getAll", "GET");
    return data;
  } catch (error) {
    console.error("Error fetching all commits:", error.message);
    throw error;
  }
};

export const getTodayCommits = async () => {
  try {
    const data = await authenticatedRequest("/commits/today", "GET");
    return data; 
  } catch (error) {
    console.error("Error fetching today's commits:", error.message);
    throw error; 
  }
};