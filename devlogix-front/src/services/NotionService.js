import { authenticatedRequest } from "./AuthService";

export const getAllNotion = async () => {
  try {
    const data = await authenticatedRequest("/notion/getAll", "GET");
    return data;
  } catch (error) {
    // console.error("Error fetching all commits:", error.message);
    // throw error;
  }
};

export const getTodayNotion = async () => {
  try {
    const data = await authenticatedRequest("/notion/today", "GET");
    return data; 
  } catch (error) {
    // console.error("Error fetching today's commits:", error.message);
    // throw error; 
  }
};