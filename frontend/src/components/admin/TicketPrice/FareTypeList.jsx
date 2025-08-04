import React, { useEffect, useState } from "react";
import { Table, Input, Spin, Button, Pagination, message, Popconfirm, Space } from "antd";
import { SearchOutlined, PlusOutlined } from "@ant-design/icons";
import { SquarePen, Trash2 } from "lucide-react";
import { CreateFareTypeModal } from "../Modal/TicketPrice/CreateFareTypeModal";
import { UpdateFareTypeModal } from "../Modal/TicketPrice/UpdateFareTypeModal";
import { fetchAllActiveFareTypesAPI, deleteFareTypeAPI } from "../../../service/TicketPriceService";
import { useLocation,useOutletContext } from "react-router-dom";

const FareTypeList = () => {
  const [fareTypes, setFareTypes] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
  const [dataFareType, setDataFareType] = useState(null);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [refreshFlag, setRefreshFlag] = useState(false); // Add state for refresh
  const size = 5;


  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname.includes("/employee/faretype-list") || location.pathname.includes("/admin/faretype-list")) {
      setBreadcrumbItems([
        { title: 'Trang chủ'},
        { title: 'Quản lý vé' },
        { title: 'Loại giá vé' },

      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);
  const fetchFareTypes = async () => {
    setLoading(true);
    try {
      const res = await fetchAllActiveFareTypesAPI();
      const responseData = res || {};
      if (responseData.result && responseData.result.length > 0) {
        const mapped = responseData.result.map((item) => ({
          id: item.id,
          name: item.name,
          basePrice: item.basePrice,
          dayPrice: item.dayPrice,
          timeSlotType: item.timeSlotType,
          movieFormat: item.movieFormat,
        }));
        setFareTypes(mapped);
      } else {
        message.warning("Không có dữ liệu loại giá vé");
      }
    } catch (err) {
      console.error("Error fetching fare types:", err);
      message.error("Lỗi tải dữ liệu: " + (err.message || "Unknown error"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFareTypes();
  }, [refreshFlag]); // Add refreshFlag as dependency

  const filteredFareTypes = fareTypes.filter((fare) =>
    fare.name.toLowerCase().includes(searchText.toLowerCase())
  );

  const pagedFareTypes = filteredFareTypes.slice((page - 1) * size, page * size);

  const handleDelete = async (id) => {
    try {
      const res = await deleteFareTypeAPI(id);
      if (res.status === 200) {
        message.success(`Xóa loại giá ${res.result.name} thành công`);
        fetchFareTypes();
      } else {
        message.error(res.message || "Xóa thất bại");
      }
    } catch (err) {
      console.error("Error deleting fare type:", err);
      message.error("Lỗi khi xóa: " + (err.message || "Unknown error"));
    }
  };

  const handleEdit = (record) => {
    setIsUpdateModalOpen(true);
    setDataFareType(record);
  };

  const columns = [
    {
      title: "STT",
      key: "index",
      render: (text, record, index) => (page - 1) * size + index + 1,
    },
    {
      title: "Lồng tiếng / Phụ đề",
      dataIndex: "name",
      key: "name",
    },
    {
      title: "Định dạng phim",
      dataIndex: "movieFormat",
      key: "movieFormat",
    },
    {
      title: "Giá cơ bản",
      dataIndex: "basePrice",
      key: "basePrice",
      render: (price) => `${price.toLocaleString()} VNĐ`,
    },
    {
      title: "Giá theo ngày",
      dataIndex: "dayPrice",
      key: "dayPrice",
      render: (price) => `${price.toLocaleString()} VNĐ`,
    },
    {
      title: "Loại khung giờ",
      dataIndex: "timeSlotType",
      key: "timeSlotType",
    },
    {
      title: "Hành động",
      key: "actions",
      render: (_, record) => (
        <Space size="large">
          <button
            className="text-blue-600 hover:text-fuchsia-500"
            onClick={() => handleEdit(record)}
          >
            <SquarePen size={16} strokeWidth={1.7} />
          </button>
          <Popconfirm
            title="Xác nhận xóa?"
            onConfirm={() => handleDelete(record.id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button type="text" danger icon={<Trash2 size={16} strokeWidth={1.7} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div
      style={{
        padding: 24,
        background: "#fff",
        borderRadius: 8,
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
      }}
    >
      <div className="flex justify-between items-center mb-4">
        <div style={{ display: "flex", alignItems: "center" }}>
          <div
            style={{
              padding: "0 12px",
              display: "flex",
              alignItems: "center",
              backgroundColor: "#f5f5f5",
              border: "1px solid #d9d9d9",
              borderRight: "none",
              borderTopLeftRadius: 6,
              borderBottomLeftRadius: 6,
              height: 40,
            }}
          >
            <SearchOutlined style={{ fontSize: 18, color: "#999" }} />
          </div>
          <Input
            size="large"
            placeholder="Tìm kiếm loại giá vé..."
            allowClear
            style={{
              width: "28vw",
              borderTopLeftRadius: 0,
              borderBottomLeftRadius: 0,
            }}
            onChange={(e) => {
              setSearchText(e.target.value);
              setPage(1);
            }}
          />
        </div>

        <Button
          type="primary"
          size="large"
          onClick={() => setIsCreateModalOpen(true)}
        >
          <PlusOutlined />
          <span>Thêm loại giá vé</span>
        </Button>
      </div>

      {loading ? (
        <div className="flex flex-col justify-center items-center gap-3 h-screen">
          <Spin size="large" />
          <span className="text-xl font-semibold">Đang tải dữ liệu...</span>
        </div>
      ) : (
        <>
          <Table
            dataSource={pagedFareTypes} 
            columns={columns}
            rowKey="id"
            pagination={false}
            bordered={false}
            style={{ backgroundColor: "#fff", border: "none" }}
            className="custom-table"
          />
          <div className="flex justify-center mt-4">
            <Pagination
              current={page}
              pageSize={size}
              total={filteredFareTypes.length}
              showSizeChanger={false}
              showTotal={(total, range) =>
                total === 0 ? "0 mục" : `${range[0]}-${range[1]} trong ${total} mục`
              }
              onChange={(current) => setPage(current)}
            />
          </div>
        </>
      )}

      <CreateFareTypeModal
        isCreateModalOpen={isCreateModalOpen}
        setIsCreateModalOpen={setIsCreateModalOpen}
        setRefreshFlag={setRefreshFlag} // Pass setRefreshFlag
      />
      <UpdateFareTypeModal
        isUpdateModalOpen={isUpdateModalOpen}
        setIsUpdateModalOpen={setIsUpdateModalOpen}
        dataFareType={dataFareType}
        setDataFareType={setDataFareType}
        setRefreshFlag={setRefreshFlag} // Pass setRefreshFlag
      />
    </div>
  );
};

export default FareTypeList;