import { useEffect, useState } from 'react'
import { fetchArtistAPI, findMovieByNameAPI } from '../../../service/TmdbService';

function ArtistList({ movieName }) {
    // console.log(movieName);
    const [artistList, setArtistList] = useState([]);

    const fetchArtist = async () => {
        try {
            const response = await findMovieByNameAPI(movieName);
            const artistList = await fetchArtistAPI(response.results[0].id);
            setArtistList(artistList.cast.slice(0, 12));
        } catch (error) {
            console.log(error);
        }
    }

    useEffect(() => {
        fetchArtist();
    }, [movieName])

    return (
        <div className='w-100 mt-5 d-flex flex-wrap'>
            <h4 className='w-100 mb-4'>Diễn viên</h4>
            { artistList.map((item, index) => {
                return (
                    <div className='mb-3' key={ index }>
                        <div
                            style={ { width: '75px', height: '75px', borderRadius: '999px', overflow: 'hidden' } }
                            className='me-3'>

                            <img
                                style={ { width: '100%', height: '100%', objectFit: 'cover' } }
                                src={ `https://image.tmdb.org/t/p/w200/${item.profile_path}` } alt="" />
                        </div>
                        <p className='fs-8 fw-medium text-center' style={ { maxWidth: '68px' } }>{ item.character }</p>
                    </div>
                )
            }) }
        </div>
    )
}

export default ArtistList