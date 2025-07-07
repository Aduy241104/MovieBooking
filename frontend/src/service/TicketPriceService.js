import axios from "../config/axios";

const fetchAllFareTypesAPI = (page, size, filter) => {
  let URL_BACKEND = `/public/fare-types?page=${page}&size=${size}&sort=id,asc`;
  if (filter !== null && filter !== undefined) {
    URL_BACKEND += `&filter=${filter}`;
  }
  return axios.get(URL_BACKEND);
};

const fetchAllActiveFareTypesAPI = () => {
  const URL_BACKEND = `/public/fare-types/getAll`;
  return axios.get(URL_BACKEND);
};

const createFareTypeAPI = (fareType) => {
  const URL_BACKEND = `/public/fare-types/create`;
  const data = {
    ...fareType,
  };
  return axios.post(URL_BACKEND, data);
};

const updateFareTypeAPI = (id, fareType) => {
  const URL_BACKEND = `/public/fare-types/update/${id}`;
  const data = {
    ...fareType,
  };
  return axios.put(URL_BACKEND, data);
};

const deleteFareTypeAPI = (id) => {
  const URL_BACKEND = `/public/fare-types/is-deleted/${id}`;
  return axios.put(URL_BACKEND);
};

const fetchAllBookingsAPI = (movieId) => {
  const URL_BACKEND = `/public/bookings/by-movie/${movieId}`;
  return axios.get(URL_BACKEND);
};

export {
  fetchAllFareTypesAPI,
  fetchAllActiveFareTypesAPI,
  createFareTypeAPI,
  updateFareTypeAPI,
  deleteFareTypeAPI,
  fetchAllBookingsAPI,
};