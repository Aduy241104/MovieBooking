import React, { useEffect, useState, useCallback } from 'react';
import { Table, Input, Modal, Space, message } from 'antd';
import { BadgePlus, Trash2, Pencil, Film } from 'lucide-react';
import { SearchOutlined } from '@ant-design/icons';
import EditTypeForm from './EditTypeForm';
import AddTypeForm from './AddTypeForm';
import 'bootstrap/dist/css/bootstrap.min.css';
import axiosClient from '../../../config/axios'

export default function TypeList() {
    const [types, setTypes] = useState([]);
    const [filteredTypes, setFilteredTypes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchText, setSearchText] = useState('');
    const [isModalVisible, setIsModalVisible] = useState(false);
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [editTypeId, setEditTypeId] = useState(null);

    const fetchTypes = useCallback(async () => {
        try {
            setLoading(true);
            const data = await axiosClient.get('/public/types');
            console.log('Thể loại:', data);
            const sorted = data.sort((a, b) => a.name.localeCompare(b.name));
            setTypes(sorted);

            const filtered = sorted.filter(t =>
                t.name.toLowerCase().includes(searchText.toLowerCase())
            );
            setFilteredTypes(filtered);
        } catch (err) {
            console.error('Lỗi khi tải thể loại:', err);
            message.error('Không thể tải danh sách thể loại.');
        } finally {
            setLoading(false);
        }
    }, [searchText]);

    useEffect(() => {
        fetchTypes();
    }, [fetchTypes]);

    const handleDelete = async (id) => {
        if (!window.confirm('Bạn có chắc muốn xóa thể loại này?')) return;
        try {
            await axiosClient.delete(`/public/types/${id}`)
            message.success("Xoá thành công.");
            fetchTypes();
        } catch (err) {
            console.error('Lỗi khi xóa thể loại:', err);
            message.error('Xoá thất bại!');
        }
    };

    const handleSearchInput = (value) => {
        setSearchText(value);
        const filtered = types.filter(t =>
            t.name.toLowerCase().includes(value.toLowerCase())
        );
        setFilteredTypes(filtered);
    };

    const columns = [
        {
            title: 'STT',
            dataIndex: 'index',
            render: (_, __, index) => index + 1,
        },
        {
            title: 'Tên thể loại',
            dataIndex: 'name',
        },
        {
            title: 'Hành động',
            align: 'center',
            render: (_, record) => (
                <Space size="middle">
                    <button className="btn btn-outline-primary btn-sm" onClick={() => {
                        setEditTypeId(record.id);
                        setEditModalVisible(true);
                    }}>
                        <Pencil size={16} />
                    </button>
                    <button className="btn btn-outline-danger btn-sm" onClick={() => handleDelete(record.id)}>
                        <Trash2 size={16} />
                    </button>
                </Space>
            )
        }
    ];

    return (
        <div className="container py-5" style={{ backgroundColor: "#ffffff", minHeight: "100vh" }}>
            <div className="d-flex justify-content-between mb-4">
                <Input
                    size="large"
                    placeholder="Tìm theo tên thể loại..."
                    addonAfter={<SearchOutlined />}
                    allowClear
                    value={searchText}
                    onChange={(e) => handleSearchInput(e.target.value)}
                    style={{ width: "30vw" }}
                />
                <button
                    className="btn d-flex"
                    style={{
                        backgroundColor: "#1677ff",
                        color: "#ffffff",
                        fontWeight: "bold",
                        fontSize: "1.1rem",
                        padding: "5px 10px",
                        marginBottom: "24px",
                        boxShadow: "0 4px 12px rgba(22, 119, 255, 0.3)"
                    }}
                    onClick={() => setIsModalVisible(true)}
                >
                    <BadgePlus strokeWidth={1.75} className="me-2" /> Thêm thể loại
                </button>
            </div>

            <Table
                columns={columns}
                dataSource={filteredTypes}
                rowKey="id"
                loading={loading}
                pagination={{ pageSize: 10, position: ['bottomCenter'] }}
            />

            <Modal
                title={<span className='d-flex'><Film className="me-2" size={20} />Thêm thể loại phim</span>}
                open={isModalVisible}
                onCancel={() => setIsModalVisible(false)}
                footer={null}
                destroyOnHidden
            >
                <AddTypeForm
                    onTypeAdded={() => {
                        fetchTypes();
                    }}
                />
            </Modal>

            <Modal
                title="Sửa thể loại phim"
                open={editModalVisible}
                onCancel={() => setEditModalVisible(false)}
                footer={null}
                destroyOnHidden
            >
                <EditTypeForm
                    typeId={editTypeId}
                    onSuccess={() => {
                        fetchTypes();
                        setEditModalVisible(false);
                    }}
                    onCancel={() => setEditModalVisible(false)}
                />
            </Modal>
        </div>
    );
}
