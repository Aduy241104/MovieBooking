import axios from "../config/axios";

const fetchAllAccountAPI = (page, size, filter) => {
    const URL_BACKEND = `/public/accounts?page=${page}&size=${size}&filter=${filter}`;
    return axios.get(URL_BACKEND);
}

const fetchAccountByIdAPI = (id) => {
    const URL_BACKEND = `/accounts/${id}`;
    return axios.get(URL_BACKEND);
}

const updateAccountAPI = (accountId, dataToUpdate) => {
    const URL_BACKEND = `/accounts`;
    const data = {
        accountId,
        ...dataToUpdate
    }
    return axios.put(URL_BACKEND, data);
}

const updateAccountDetailAPI = (accountId, dataToUpdate) => {
    const URL_BACKEND = `/accounts/details`;
    const data = {
        accountId,
        ...dataToUpdate
    }
    return axios.put(URL_BACKEND, data);
}

const updateStatusAccountAPI = (accountId, active) => {
    const URL_BACKEND = `/accounts/is-active`;
    const data = {
        accountId,
        active
    }
    return axios.put(URL_BACKEND, data);
}

const createAccountAPI = (roleId, email, fullName, gender, password, phone, birthday) => {
    const URL_BACKEND = `/accounts`;
    const data = {
        role: {
            roleId
        },
        email,
        fullName,
        gender,
        password,
        phone,
        birthday
    }
    return axios.post(URL_BACKEND, data);
}

export {
    fetchAllAccountAPI,
    fetchAccountByIdAPI,
    updateAccountAPI,
    updateAccountDetailAPI,
    updateStatusAccountAPI,
    createAccountAPI,

}