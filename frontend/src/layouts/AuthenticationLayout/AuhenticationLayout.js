function AuhenticationLayout({ children }) {
    return (
        <div className="container-fluid backroud-image-2 d-flex justify-content-center align-items-center">
            <div className="wrappersss text-light rounded-2 border border-1 border-secondary p-5">
                { children }
            </div>
        </div>
    )
}

export default AuhenticationLayout