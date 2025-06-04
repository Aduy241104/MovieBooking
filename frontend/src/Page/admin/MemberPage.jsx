import { SearchOutlined, UserAddOutlined } from '@ant-design/icons';
import { Button, Input, Select } from "antd";
import { debounce } from 'lodash';
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { sfAnd, sfEqual, sfLike, sfOr } from 'spring-filter-query-builder';
import { UserTable } from '../../components/admin/Table/UserTable';

export const MemberPage = () => {
    const { setFilter } = useOutletContext();

    const { setBreadcrumbItems } = useOutletContext();
    const [searchUser, setSearchUser] = useState("");
    const [filterStatus, setFilterStatus] = useState(null);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

    useEffect(() => {
        setBreadcrumbItems([
            { title: 'Trang chủ', href: '/admin' },
            { title: 'Người dùng' },
            { title: 'Thành viên' },
        ]);
    }, []);

    useEffect(() => {
        const handleFilterMember = debounce(() => {
            const filters = [sfEqual('role.roleName', 'Member')];
            if (searchUser) {
                filters.push(
                    sfOr([
                        sfLike('email', `*${searchUser}*`),
                        sfLike('fullName', `*${searchUser}*`)
                    ])
                );
            }
            if (filterStatus !== null && filterStatus !== undefined) {
                filters.push(sfEqual('active', filterStatus));
            }
            const filter = sfAnd(filters);
            setFilter(filter.toString());
        }, 500);

        return () => handleFilterMember.cancel();
    }, [searchUser, filterStatus]);



    return (
        <>
            <div className='flex justify-between mb-6'>
                <div className='flex gap-8'>
                    <Input style={{ width: "30vw" }}
                        size='large'
                        addonBefore={<SearchOutlined />}
                        placeholder="Tìm kiếm tài khoản..."
                        allowClear
                        onChange={(value) => setSearchUser(value.target.value)}
                    />
                    <Select
                        size='large'
                        // defaultValue="Trạng thái"
                        style={{ width: "10vw" }}
                        options={[
                            { value: false, label: 'Hoạt động' },
                            { value: true, label: 'Đã khoá' }
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
                    <span>Thêm thành viên</span>
                </Button>
            </div>

            <UserTable />
        </>
    );
}