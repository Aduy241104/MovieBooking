import Sidebar from './Sidebar'

function ProfileLayout({ children }) {
    return (
        <div className="flex min-h-screen text-light bg-midnight">
            <Sidebar />
            <div className="flex-1 p-4">
                <h1>hêllo</h1>
            </div>
        </div>
    )
}

export default ProfileLayout