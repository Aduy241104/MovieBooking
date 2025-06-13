import axios from "../config/axios";

const fetchAllPromotionAPI = (page, size, filter) => {
    let URL_BACKEND = `/promotions?page=${page}&size=${size}&sort=id,asc`;
    // Chỉ thêm filter vào URL khi filter có giá trị hợp lệ
    if (filter !== null && filter !== undefined) {
        URL_BACKEND += `&filter=${filter}`;
    }
    return axios.get(URL_BACKEND);
}

const createPromotionAPI = (promotion) => {
    const URL_BACKEND = `/promotions`;
    const data = {
        ...promotion
    }
    return axios.post(URL_BACKEND, data);
}

const updatePromotionAPI = (id, promotion) => {
    const URL_BACKEND = `/promotions`;
    const data = {
        id,
        ...promotion
    }
    return axios.put(URL_BACKEND, data);
}

const updatePromotionActiveAPI = (id, active) => {
    const URL_BACKEND = `/promotions/active`;
    const data = {
        id,
        active
    }
    return axios.put(URL_BACKEND, data);
}

const deletePromotionAPI = (id) => {
    const URL_BACKEND = `/promotions/is-deleted`;
    const data = {
        id,
        isDelete: true
    }
    return axios.put(URL_BACKEND, data);
}


export {
    fetchAllPromotionAPI,
    createPromotionAPI,
    updatePromotionAPI,
    updatePromotionActiveAPI,
    deletePromotionAPI
};