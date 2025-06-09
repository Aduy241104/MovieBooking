import axios from "../config/axios";

const fetchAllAccountAPI = (page, size, filter) => {
    const URL_BACKEND = `/public/accounts?page=${page}&size=${size}&filter=${filter}&sort=accountId,asc`;
    return axios.get(URL_BACKEND);
}

const fetchAccountByIdAPI = (accountId) => {
    const URL_BACKEND = `public/accounts/${accountId}`;
    return axios.get(URL_BACKEND);
}

const updateAccountQuickAPI = (accountId, dataToUpdate) => {
    const URL_BACKEND = `/public/accounts`;
    const data = {
        accountId,
        ...dataToUpdate
    }
    return axios.put(URL_BACKEND, data);
}

const updateAccountInfoAPI = (accountId, dataToUpdate) => {
    const URL_BACKEND = `/public/accounts/${accountId}`;
    const data = {
        accountId,
        ...dataToUpdate
    }
    return axios.put(URL_BACKEND, data);
}

const updateAccountStatusAPI = (accountId, status) => {
    const URL_BACKEND = `/public/accounts/status`;
    const data = {
        accountId,
        status
    }
    return axios.put(URL_BACKEND, data);
}

const createAccountAPI = (roleId, email, fullName, gender, password, phoneNumber, dateOfBirth) => {
    const URL_BACKEND = `/public/accounts`;
    const data = {
        role: {
            roleId
        },
        email,
        fullName,
        gender,
        password,
        phoneNumber,
        dateOfBirth
    }
    return axios.post(URL_BACKEND, data);
}

const deleteAccountAPI = (accountId) => {
    const URL_BACKEND = `/public/accounts/is-deleted`;
    const data = {
        accountId,
        isDelete: true
    }
    return axios.put(URL_BACKEND, data);
}

const updateAccountAvatarAPI = (accountId, file) => {
    const URL_BACKEND = `/public/accounts/${accountId}/avatar`;
    const formData = new FormData();
    formData.append('avatar', file);
    return axios.put(URL_BACKEND, formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}

export {
    fetchAllAccountAPI,
    fetchAccountByIdAPI,
    updateAccountQuickAPI,
    updateAccountInfoAPI,
    updateAccountStatusAPI,
    createAccountAPI,
    deleteAccountAPI,
    updateAccountAvatarAPI
}