
//Locpng
import axiosInstance from '../config/axiosBooking';

const API_URL = '/public/screenings'; // Endpoint public

export const getMovieSchedules = (movieId) => {
      console.log(`Fetching schedules for movieId: ${movieId}`); // THÊM DÒNG NÀY
    return axiosInstance.get(`${API_URL}/movie/${movieId}`);
};

export const getSeatStatus = (screeningId) => {
    return axiosInstance.get(`${API_URL}/${screeningId}/seat-status`);
};