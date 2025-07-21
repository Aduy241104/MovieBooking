import { useEffect, useState } from 'react';
import { fetchArtistAPI, findMovieByNameAPI } from '../../../service/TmdbService';

function ArtistList({ movieName }) {
    const [artistList, setArtistList] = useState([]);
    const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);

    // Theo dõi resize để responsive
    useEffect(() => {
        const handleResize = () => {
            setIsMobile(window.innerWidth <= 768);
        };

        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    // Fetch diễn viên
    const fetchArtist = async () => {
        try {
            const response = await findMovieByNameAPI(movieName);
            const artistList = await fetchArtistAPI(response.results[0].id);
            setArtistList(artistList.cast.slice(0, 12));
        } catch (error) {
            console.log(error);
        }
    };

    useEffect(() => {
        fetchArtist();
    }, [movieName]);

    return (
        <div
            className="w-100 mt-5 d-flex flex-wrap"
            style={ {
                justifyContent: isMobile ? 'center' : 'flex-start',
                gap: isMobile ? '1rem' : '0.5rem'
            } }
        >
            <h4 className="w-100 mb-4">Diễn viên</h4>

            { artistList.map((item, index) => (
                <div key={ index } style={ { textAlign: 'center', marginBottom: '1rem' } }>
                    <div
                        style={ {
                            width: isMobile ? '50px' : '75px',
                            height: isMobile ? '50px' : '75px',
                            borderRadius: '999px',
                            overflow: 'hidden',
                            margin: '0 auto'
                        } }
                        className="me-3"
                    >
                        <img
                            src={ `https://image.tmdb.org/t/p/w200/${item.profile_path}` }
                            alt=""
                            style={ {
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover'
                            } }
                        />
                    </div>
                    <p
                        className="fs-8 fw-medium"
                        style={ {
                            maxWidth: isMobile ? '60px' : '75px',
                            margin: '0 auto'
                        } }
                    >
                        { item.character }
                    </p>
                </div>
            )) }
        </div>
    );
}

export default ArtistList;
