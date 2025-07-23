// import React, { useEffect, useState, useCallback } from "react";
// import { useLocation, useNavigate, useOutletContext } from "react-router-dom";
// import { SquarePlus } from "lucide-react";
// import { Table, Input, Button, notification } from "antd";
// import { SearchOutlined } from "@ant-design/icons";
// import axios from "axios";
// import "bootstrap/dist/css/bootstrap.min.css";

// const RoomList = () => {
//   const [rooms, setRooms] = useState([]);
//   const [filteredRooms, setFilteredRooms] = useState([]);
//   const [searchValue, setSearchValue] = useState("");
//   const [loading, setLoading] = useState(false);

//   const navigate = useNavigate();
//   const location = useLocation();
//   const { setBreadcrumbItems } = useOutletContext();
//   const token = localStorage.getItem("token");

//   useEffect(() => {
//     if (location.pathname === "/admin/room-list") {
//       setBreadcrumbItems([
//         { title: "Trang chủ", href: "/admin" },
//         { title: "Phòng chiếu" },
//       ]);
//     }
//   }, [location.pathname, setBreadcrumbItems]);

//   const fetchRooms = useCallback(async () => {
//     try {
//       setLoading(true);
//       const res = await axios.get("http://localhost:8081/api/rooms", {
//         headers: {
//           Authorization: `Bearer ${token}`,
//           "Content-Type": "application/json",
//         },
//       });
//       const sortedRooms = res.data.sort((a, b) => a.cinemaRoomId - b.cinemaRoomId);
//       setRooms(sortedRooms);
//       setFilteredRooms(
//         sortedRooms.filter((room) =>
//           room.cinemaRoomName.toLowerCase().includes(searchValue.toLowerCase())
//         )
//       );
//     } catch (err) {
//       console.error("Lỗi khi load danh sách phòng:", err);
//       notification.error({
//         message: "TẢI DỮ LIỆU THẤT BẠI",
//         description: "Không thể tải danh sách phòng. Vui lòng thử lại.",
//       });
//     } finally {
//       setLoading(false);
//     }
//   }, [token, searchValue]);

//   useEffect(() => {
//     fetchRooms();
//   }, [fetchRooms]);

//   const handleSearch = (value) => {
//     setSearchValue(value);
//     const filtered = rooms.filter((room) =>
//       room.cinemaRoomName.toLowerCase().includes(value.toLowerCase())
//     );
//     setFilteredRooms(filtered);
//   };

//   const columns = [
//     {
//       title: "STT",
//       render: (_, __, index) => index + 1,
//       width: 80,
//     },
//     {
//       title: "Tên phòng chiếu",
//       dataIndex: "cinemaRoomName",
//       render: (text, record) => (
//         <span
//           className="cursor-pointer hover:underline"
//           onClick={() => navigate(`/admin/room-list/room/${record.cinemaRoomId}`)}
//         >
//           {text}
//         </span>
//       ),
//     },
//   ];

//   return (
//     <div
//       className="container py-5"
//       style={{
//         backgroundColor: "#ffffff",
//         minHeight: "100vh",
//         borderRadius: "16px",
//         boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
//         overflow: "hidden",
//       }}
//     >
//       <div className="d-flex justify-content-between mb-4">
//         <Input
//           size="large"
//           placeholder="Tìm kiếm phòng chiếu..."
//           addonAfter={<SearchOutlined />}
//           allowClear
//           value={searchValue}
//           onChange={(e) => handleSearch(e.target.value)}
//           style={{ width: "30vw" }}
//         />

//         <button
//           className="btn d-flex"
//           style={{
//             backgroundColor: "#1677ff",
//             color: "#ffffff",
//             fontWeight: "bold",
//             fontSize: "1.1rem",
//             padding: "5px 10px",
//             marginBottom: "24px",
//             boxShadow: "0 4px 12px rgba(22, 119, 255, 0.3)",
//           }}
//           onClick={() => navigate("/admin/room-list/add-room")}
//         >
//           <SquarePlus strokeWidth={1.75} className="me-2" /> Thêm phòng
//         </button>
//       </div>

//       <Table
//         columns={columns}
//         dataSource={filteredRooms}
//         rowKey="cinemaRoomId"
//         loading={loading}
//         pagination={{ pageSize: 10, position: ["bottomCenter"] }}
//         onRow={(record) => ({
//           onClick: () => navigate(`/admin/room-list/room/${record.cinemaRoomId}`),
//         })}
//       />
//     </div>
//   );
// };

// export default RoomList;
import React, { useEffect, useState, useCallback, useMemo } from "react";
import { useLocation, useNavigate, useOutletContext } from "react-router-dom";
import { Table, Input, Button, notification } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { SquarePlus } from "lucide-react";
import axios from "axios";
import "bootstrap/dist/css/bootstrap.min.css";

const RoomList = () => {
  const [rooms, setRooms] = useState([]);
  const [searchValue, setSearchValue] = useState("");
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();
  const token = localStorage.getItem("token");

  // Cập nhật breadcrumb nếu đang ở /admin/room-list
  useEffect(() => {
    if (location.pathname === "/admin/room-list") {
      setBreadcrumbItems([
        { title: "Trang chủ", href: "/admin" },
        { title: "Phòng chiếu" },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  // Lấy danh sách phòng
  const fetchRooms = useCallback(async () => {
    try {
      setLoading(true);
      const res = await axios.get("http://localhost:8081/api/rooms", {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      const sortedRooms = res.data.sort(
        (a, b) => a.cinemaRoomId - b.cinemaRoomId
      );
      setRooms(sortedRooms);
    } catch (err) {
      console.error("Lỗi khi load danh sách phòng:", err);
      notification.error({
        message: "TẢI DỮ LIỆU THẤT BẠI",
        description: "Không thể tải danh sách phòng. Vui lòng thử lại.",
      });
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    fetchRooms();
  }, [fetchRooms]);

  // Lọc phòng dựa vào tìm kiếm
  const filteredRooms = useMemo(() => {
  return rooms.filter((room) =>
    (room.cinemaRoomName || "")
      .toLowerCase()
      .includes(searchValue.toLowerCase())
  );
}, [rooms, searchValue]);

  const handleSearch = (value) => {
    setSearchValue(value);
  };

  const navigateToRoomDetail = (id) => {
    navigate(`/admin/room-list/room/${id}`);
  };

  const columns = useMemo(
    () => [
      {
        title: "STT",
        render: (_, __, index) => index + 1,
        width: 50,
        align: "center",
      },
      {
        title: "Tên phòng chiếu",
        dataIndex: "cinemaRoomName",
         width: 320,
        render: (text, record) => (
          <span
            className="cursor-pointer hover:underline"
            onClick={() => navigateToRoomDetail(record.cinemaRoomId)}
          >
            {text}
          </span>
        ),
      },
      {
        title: "Tổng ghế",
        dataIndex: "seatQuantity",
        align: "center",
        render: (value) => value ?? "—",
        width: 120,
      },
      
    ],
    []
  );

  return (
    <div
      className="container py-5"
      style={{
        backgroundColor: "#ffffff",
        minHeight: "100vh",
        borderRadius: "16px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
        overflow: "hidden",
      }}
    >
      <div className="d-flex justify-content-between mb-4">
        <Input
          size="large"
          placeholder="Tìm kiếm phòng chiếu..."
          addonAfter={<SearchOutlined />}
          allowClear
          value={searchValue}
          onChange={(e) => handleSearch(e.target.value)}
          style={{ width: "30vw" }}
        />

        <Button
          type="primary"
          size="large"
          icon={<SquarePlus strokeWidth={1.75} />}
          style={{ fontWeight: "bold", fontSize: "1.1rem" }}
          onClick={() => navigate("/admin/room-list/add-room")}
        >
          Thêm phòng
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={filteredRooms}
        rowKey="cinemaRoomId"
        loading={loading}
        pagination={{ pageSize: 10, position: ["bottomCenter"] }}
        onRow={(record) => ({
          onClick: () => navigateToRoomDetail(record.cinemaRoomId),
        })}
      />
    </div>
  );
};

export default RoomList;
