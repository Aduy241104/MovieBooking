import { Footer } from "antd/es/layout/layout";


export const AdminFooter = () => {
    return (
        <>
            <Footer style={{
                textAlign: 'center',
                color: '#666',
                fontSize: '12px',
                background: '#f0f2f5'
            }}>
                Admin Dashboard ©{new Date().getFullYear()} Created with QuangDucky
            </Footer>
        </>
    );
}