import { useEffect, useState } from 'react'
import { fetchArtistAPI, findMovieByNameAPI } from '../service/TmdbService'

function Artist() {

    const [artistList, setArtistList] = useState([]);



    const fetchArtist = async () => {
        try {
            const response = await findMovieByNameAPI("Mission: Impossible - The Final Reckoning");
            console.log("phim: ", response);
            console.log("id:", response.results[0].id);

            const artistList = await fetchArtistAPI(response.results[0].id);
            console.log("danh sách diễn  viên: ", artistList);
            setArtistList(artistList.cast.slice(0, 20));

        } catch (error) {
            console.log(error);
        }
    }

    useEffect(() => {
        fetchArtist();
    }, [])
    return (
        <div>
            { artistList.map((item, index) => {
                return (
                    <div
                        key={ index }
                        style={ { width: '60px', height: '60px', borderRadius: '999px', overflow: 'hidden' } }>
                        <img
                            style={ { width: '100%', height: '100%', objectFit: 'cover' } }
                            src={ `https://image.tmdb.org/t/p/w200/${item.profile_path}` } alt="" />
                    </div>
                )
            }) }
        </div>
    )
}

export default Artist