import { useEffect, useState } from 'react'
import { fetchArtistAPI, findMovieByNameAPI } from '../../../service/TmdbService';

function ArtistList({ movieName }) {
    // console.log(movieName);
    const [artistList, setArtistList] = useState([]);

    const fetchArtist = async () => {
        try {
            const response = await findMovieByNameAPI(movieName);
            // console.log("phim: ", response);
            // console.log("id:", response.results[0].id);

            const artistList = await fetchArtistAPI(response.results[0].id);
            // console.log("danh sách diễn  viên: ", artistList);
            setArtistList(artistList.cast.slice(0, 12));

        } catch (error) {
            console.log(error);
        }
    }

    useEffect(() => {
        fetchArtist();
    }, [movieName])

    return (
        <div className='w-100 d-flex flex-wrap'>
            <p className='w-100 mb-4 text-red'>Diễn viên</p>
            { artistList.map((item, index) => {
                return (
                    <div className='mb-3'>
                        <div
                            key={ index }
                            style={ { width: '75px', height: '75px', borderRadius: '999px', overflow: 'hidden' } }
                            className='me-3'>

                            <img
                                style={ { width: '100%', height: '100%', objectFit: 'cover' } }
                                src={ `https://image.tmdb.org/t/p/w200/${item.profile_path}` } alt="" />
                        </div>
                        <p className='fs-8 text-center' style={ { maxWidth: '60px' } }>{ item.character }</p>
                    </div>
                )
            }) }
        </div>
    )
}

export default ArtistList