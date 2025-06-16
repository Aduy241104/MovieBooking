import { useEffect, useState } from "react"


function GoToTop() {
    const [isShow, setShow] = useState(false);

    useEffect(() => {
        const showGoToTop = () => {
            if (window.scrollY >= 340) {
                setShow(true);
            } else {
                setShow(false)
            }
        }

        window.addEventListener('scroll', showGoToTop);
        return () => { window.removeEventListener('scroll', showGoToTop) }
    }, [])

    return (
        <div>
            { isShow && (
                <button
                    onClick={ () => window.scrollTo({ top: 0, behavior: 'smooth' }) }
                    className="btn btn-light border rounded-5 shadow-sm"
                    style={ {
                        position: 'fixed',
                        bottom: '20px',
                        right: '20px',
                        width: '47px',
                        height: '47px',
                        padding: 0,
                        fontSize:'16px',
                        fontWeight:'bold',
                        display: 'flex',
                        justifyContent: 'center',
                        flexDirection :'column'
                    } }
                >
                    <i className="fa-solid fa-arrow-up"></i>
                </button>
            ) }
        </div>
    
    )
}

export default GoToTop