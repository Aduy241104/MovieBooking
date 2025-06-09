import { LoadingOutlined, SearchOutlined, UserAddOutlined } from '@ant-design/icons';
import { Button, Input, Select, Spin } from "antd";
import { debounce } from 'lodash';
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { sfAnd, sfEqual, sfLike, sfLower, sfOr } from 'spring-filter-query-builder';
import { UserTable } from '../../components/admin/Table/UserTable';
import { CreateUserModal } from '../../components/admin/Modal/users/CreateUserModal';

export const UserPage = (props) => {
    const { setFilter, setBreadcrumbItems, isLoading, setIsLoading } = useOutletContext();
    const { userText, userFilter } = props;

    const userRole = userFilter;

    const [searchUser, setSearchUser] = useState("");
    const [filterStatus, setFilterStatus] = useState(null);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isInitialized, setIsInitialized] = useState(false);

    useEffect(() => {
        setBreadcrumbItems([
            { title: 'Trang chủ', href: '/admin' },
            { title: 'Người dùng' },
            { title: `${userText}` },
        ]);

        // Set filter ban đầu
        const initialFilter = sfEqual('role.roleName', `${userFilter}`);
        setFilter(initialFilter.toString());
        setIsInitialized(true);
    }, [userFilter]);

    useEffect(() => {
        // Chỉ set loading cho search/filter, không phải lần đầu mount
        if (!isInitialized) return;

        const handleFilterMember = debounce(() => {
            setIsLoading(true);
            const filters = [sfEqual('role.roleName', `${userFilter}`)];
            const searchTerm = searchUser ? searchUser.toLowerCase() : "";
            if (searchUser) {
                filters.push(
                    sfOr([
                        sfLike(sfLower('email'), `*${searchTerm}*`),
                        sfLike(sfLower('fullName'), `*${searchTerm}*`)
                    ])
                );
            }
            if (filterStatus !== null && filterStatus !== undefined) {
                filters.push(sfEqual('status', filterStatus));
            }
            const filter = sfAnd(filters);
            setFilter(filter.toString());
        }, 500);

        // Chỉ gọi khi thực sự có search hoặc filter
        if (searchUser || filterStatus !== null) {
            handleFilterMember();
        }

        return () => handleFilterMember.cancel();
    }, [searchUser, filterStatus, isInitialized]);

    return (
        <>
            <div style={{
                padding: 24,
                // minHeight: 360,
                background: '#fff',
                borderRadius: '8px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            }}>
                <div className='flex justify-between mb-4'>
                    <div style={{ display: "flex", gap: "2rem" }}>
                        <Input style={{ width: "30vw" }}
                            size='large'
                            addonBefore={<SearchOutlined />}
                            placeholder="Tìm kiếm tài khoản..."
                            allowClear
                            onChange={(value) => setSearchUser(value.target.value)}
                        />
                        <Select
                            size='large'
                            style={{ width: "10vw" }}
                            options={[
                                { value: 1, label: 'Hoạt động' },
                                { value: 0, label: 'Đã khoá' }
                            ]}
                            placeholder="Trạng thái"
                            allowClear
                            onChange={(value) => setFilterStatus(value)}
                        />
                    </div>
                    <Button onClick={() => setIsCreateModalOpen(true)}
                        size='large'
                        type="primary"
                    >
                        <UserAddOutlined />
                        <span>Thêm {userText}</span>
                    </Button>
                </div>

                {isLoading ? (
                    <div className='flex flex-col justify-center items-center gap-3 h-screen'>
                        <Spin indicator={<LoadingOutlined spin />} size="large" />
                        <span className='text-xl font-semibold'>Đang tải dữ liệu...</span>
                    </div>
                ) : (
                    <UserTable
                        userText={userText}
                        userRole={userRole}
                    />
                )}

            </div>


            <CreateUserModal
                isCreateModalOpen={isCreateModalOpen}
                setIsCreateModalOpen={setIsCreateModalOpen}
                userText={userText}
                userRole={userRole}
            />
        </>
    );
}