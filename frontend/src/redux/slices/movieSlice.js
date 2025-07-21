import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { getNowShowingMovieAPI, getUpComingMovieAPI } from '../../service/TheMovieService'

const fetchNowPlaying = createAsyncThunk('movie/fetchNowPlaying', async () => {
    const res = await getNowShowingMovieAPI();
    return res.result;
})

const fetchComingSoon = createAsyncThunk('movie/fetchComingSoon', async () => {
    const res = await getUpComingMovieAPI();
    return res.result;
})

const initialState = {
    nowPlaying: {
        movies: [],
        status: 'idle',
        error: null
    },
    comingSoon: {
        movies: [],
        status: 'idle',
        error: null
    }
}

const movieSlice = createSlice({
    name: 'movie',
    initialState,
    reducers: {
        clearNowPlaying(state) {
            state.nowPlaying.movies = [];
            state.nowPlaying.status = 'idle';
            state.nowPlaying.error = null;
        },
        clearComingSoon(state) {
            state.comingSoon.movies = [];
            state.comingSoon.status = 'idle';
            state.comingSoon.error = null;
        }
    },
    extraReducers: (builder) => {
        builder

            //now playing
            .addCase(fetchNowPlaying.pending, (state) => {
                state.nowPlaying.status = 'loading';
            })

            .addCase(fetchNowPlaying.fulfilled, (state, action) => {
                state.nowPlaying.status = 'succeeded';
                state.nowPlaying.movies = action.payload;
            })

            .addCase(fetchNowPlaying.rejected, (state, action) => {
                state.nowPlaying.status = 'failed';
                state.nowPlaying.error = action.error.message;
            })


            //coming soon
            .addCase(fetchComingSoon.pending, (state) => {
                state.comingSoon.status = "loading";
            })

            .addCase(fetchComingSoon.fulfilled, (state, action) => {
                state.comingSoon.status = "succeeded";
                state.comingSoon.movies = action.payload;
            })

            .addCase(fetchComingSoon.rejected, (state, action) => {
                state.comingSoon.status = "failed";
                state.comingSoon.error = action.error.message;

            })
    }
})

export { fetchNowPlaying, fetchComingSoon }
export const { clearNowPlaying, clearComingSoon } = movieSlice.actions;
export default movieSlice.reducer;