import axios from "axios";

const TMDB_BASE_URL = "https://api.themoviedb.org/3";
const API_TOKEN_TMDB = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3YTQwMGI1ZWU3MzNmMzJmZjJlYzYwMGM0MTU4ZTRlMSIsIm5iZiI6MTc0NTI4Nzg5MC4yMjg5OTk5LCJzdWIiOiI2ODA2ZmFkMjZlMWE3NjllODFlZTdkZDgiLCJzY29wZXMiOlsiYXBpX3JlYWQiXSwidmVyc2lvbiI6MX0.Cqcr5MarGa95x8YwLrEFvBnAZ5cuG5BIPhqGb3gRshQ";

const tmdbAxios = axios.create({
    baseURL: TMDB_BASE_URL,
    headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${API_TOKEN_TMDB}`,
    },
});
        
export const findMovieByNameAPI = async (movieName) => {
    try {
        const response = await tmdbAxios.get("/search/movie", {
            params: {
                query: movieName,
                include_adult: false,
                language: "en-US",
                page: 1,
            },
        });
        return response.data;
    } catch (error) {
        console.error("Lỗi khi gọi TMDB API:", error);
        return [];
    }
};

export const fetchArtistAPI = async (movieId) => {
    try {
        const response = await tmdbAxios.get(`/movie/${movieId}/credits?language=en-US`)
        return response.data;
    } catch (error) {
        console.error("Lỗi khi gọi TMDB API:", error);
        return [];
    }
}
