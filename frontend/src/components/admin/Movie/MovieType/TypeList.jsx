import React, { useEffect, useState, useCallback } from 'react';
import {
  Table,
  Input,
  Modal,
  Space,
  Popconfirm,
  notification,
} from 'antd';
import { BadgePlus, Trash2, SquarePen, Film } from 'lucide-react';
import { SearchOutlined } from '@ant-design/icons';
import EditTypeForm from './EditTypeForm';
import AddTypeForm from './AddTypeForm';
import 'bootstrap/dist/css/bootstrap.min.css';
import axiosClient from '../../../../config/axios';
import { useLocation, useOutletContext } from 'react-router-dom';

export default function TypeList() {
  const [types, setTypes] = useState([]);
  const [filteredTypes, setFilteredTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchText, setSearchText] = useState('');
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editTypeId, setEditTypeId] = useState(null);
  const { setBreadcrumbItems } = useOutletContext();
  const location = useLocation();

    // Cập nhật breadcrumb nếu đang ở /admin/room-list
    useEffect(() => {
      if (location.pathname === "/admin/movie-type") {
        setBreadcrumbItems([
          { title: "Trang chủ" },
          { title: "Quản lý thể loại phim" },
        ]);
      }
    }, [location.pathname, setBreadcrumbItems]);

  const fetchTypes = useCallback(async () => {
    try {
      setLoading(true);
      const data = await axiosClient.get('/types');

      // Sắp xếp theo thứ tự id tăng dần (thứ tự thêm vào)
      const sorted = data.sort((a, b) => a.id - b.id);

      setTypes(sorted);

      const filtered = sorted.filter((t) =>
        t.name.toLowerCase().includes(searchText.toLowerCase())
      );
      setFilteredTypes(filtered);
    } catch (err) {
      console.error('Lỗi khi tải thể loại:', err);
      notification.error({
        message: 'TẢI DỮ LIỆU THẤT BẠI',
        description: 'Không thể tải danh sách thể loại. Vui lòng thử lại.',
      });
    } finally {
      setLoading(false);
    }
  }, [searchText]);

  useEffect(() => {
    fetchTypes();
  }, [fetchTypes]);

  const handleDelete = async (id) => {
    try {
      await axiosClient.delete(`/types/${id}`);
      notification.success({
        message: 'XOÁ THÀNH CÔNG',
        description: 'Thể loại đã được xoá thành công.',
      });
      fetchTypes();
    } catch (err) {
      console.error('Lỗi khi xóa thể loại:', err);
      notification.error({
        message: 'XOÁ THẤT BẠI',
        description: 'Không thể xoá thể loại. Vui lòng thử lại.',
      });
    }
  };

  const handleSearchInput = (value) => {
    setSearchText(value);
    const filtered = types.filter((t) =>
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
          <button
            className="text-blue-600 hover:text-fuchsia-500"
            onClick={() => {
              setEditTypeId(record.id);
              setEditModalVisible(true);
            }}
          >
            <SquarePen size={16} strokeWidth={1.7} />
          </button>

          <Popconfirm
            title="Xác nhận xoá thể loại"
            description="Bạn có chắc muốn xoá thể loại này không?"
            onConfirm={() => handleDelete(record.id)}
            okText="Xoá"
            cancelText="Huỷ"
            placement="topLeft"
          >
            <button className="text-amber-600 hover:text-amber-700">
              <Trash2 size={16} strokeWidth={1.7} />
            </button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div
      className="container py-5"
      style={{
        backgroundColor: '#ffffff',
        minHeight: '100vh',
        borderRadius: '16px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.05)',
        overflow: 'hidden',
      }}
    >
      <div className="d-flex justify-content-between mb-4">
        <Input
          size="large"
          placeholder="Tìm theo tên thể loại..."
          addonAfter={<SearchOutlined />}
          allowClear
          value={searchText}
          onChange={(e) => handleSearchInput(e.target.value)}
          style={{ width: '30vw' }}
        />

        <button
          className="btn d-flex"
          style={{
            backgroundColor: '#1677ff',
            color: '#ffffff',
            fontWeight: 'bold',
            fontSize: '1.1rem',
            padding: '5px 10px',
            marginBottom: '24px',
            boxShadow: '0 4px 12px rgba(22, 119, 255, 0.3)',
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
        title={
          <span className="d-flex">
            <Film className="me-2" size={20} /> Thêm thể loại phim
          </span>
        }
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={null}
        destroyOnHidden
      >
        <AddTypeForm
          onTypeAdded={fetchTypes}
          onSuccessClose={() => setIsModalVisible(false)}
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
