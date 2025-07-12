import Sidebar from "./Sidebar";
import DefaultLayout from "../DefaultLayout";
import { Outlet } from "react-router-dom";

function ProfileLayout() {
    return (
        <DefaultLayout>
            <div className="pt-3"></div>
            <div className="flex min-h-screen text-light bg-midnight pt-5">
                <Sidebar />
                <div className="flex-1 p-4">
                    <Outlet />
                </div>
            </div>
        </DefaultLayout>
    );
}
export default ProfileLayout;
