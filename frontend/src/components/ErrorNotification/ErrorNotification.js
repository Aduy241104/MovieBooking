
function ErrorNotification({ children }) {
    return (
        children && children.length > 0 ? (
            <div className="bg-warning w-100 p-2 text-dark mt-2 mb-2 rounded-1">
                <p><i className="fa-solid fa-circle-exclamation"></i> { children }</p>
            </div>
        ) : null
    );
}

export default ErrorNotification;
