import { configureStore } from "@reduxjs/toolkit";
import movieReducer from './slices/movieSlice'

const store = configureStore({
    reducer: {
        movie: movieReducer
    }
});

export default store;