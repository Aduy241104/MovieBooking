import {
  Button,
  Form,
  Input,
  DatePicker,
  Select,
  Upload,
  Row,
  Col,
  notification,
} from "antd";
import {
  PlusOutlined,
  UploadOutlined,
  CloseOutlined,
} from "@ant-design/icons";
import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import moment from "moment";

export default function MovieForm({ movieId, onSuccess }) {
  const [form] = Form.useForm();
  const navigate = useNavigate();

  const [types, setTypes] = useState([]);
  const [smallImageFile, setSmallImageFile] = useState(null);
  const [largeImageFile, setLargeImageFile] = useState(null);
  const [previewSmallImage, setPreviewSmallImage] = useState(null);
  const [previewLargeImage, setPreviewLargeImage] = useState(null);

  const isEdit = !!movieId;
  const token = localStorage.getItem("token");

  const getFullImageUrl = (path) => {
    if (!path) return null;

    const isFullUrl = path.startsWith("http");
    if (isFullUrl) {
      return path;
    }

    return `http://localhost:8081${path}`;
  };
  useEffect(() => {
    // Load movie types
    axios
      .get("http://localhost:8081/api/types", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => setTypes(res.data))
      .catch(() => notification.error({ message: "Không tải được danh sách thể loại." }));
  }, [token]);

  useEffect(() => {
    if (!isEdit) return;

    axios
      .get(`http://localhost:8081/api/movies/${movieId}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        const m = res.data;
        form.setFieldsValue({
          ...m,
          fromDate: moment(m.fromDate),
          toDate: moment(m.toDate),
          trailerLink: m.trailerUrl || "",
          typeIds: m.typeIds || [],
        });
        setPreviewSmallImage(getFullImageUrl(m.smallImageUrl));
        setPreviewLargeImage(getFullImageUrl(m.largeImageUrl));
      })
      .catch(() =>
        notification.error({ message: "Không thể tải thông tin phim để chỉnh sửa." })
      );
  }, [form, movieId, isEdit, token]);

  const normalizeYouTubeUrl = (url) => {
    if (!url) return "";
    try {
      const match = url.match(
        /(?:youtu\.be\/|youtube\.com\/(?:watch\?v=|embed\/|v\/))([a-zA-Z0-9_-]{11})/
      );
      return match ? `https://www.youtube.com/embed/${match[1]}` : url;
    } catch {
      return url;
    }
  };

  const handleSubmit = async (values) => {
    const data = new FormData();

    const payload = {
      ...values,
      trailerLink: normalizeYouTubeUrl(values.trailerLink),
    };

    for (const [key, value] of Object.entries(payload)) {
      if (key === "fromDate" || key === "toDate") {
        data.append(key, value.format("YYYY-MM-DD"));
      } else if (key === "typeIds") {
        value.forEach((id) => data.append("typeIds", id));
      } else {
        data.append(key, value);
      }
    }

    if (smallImageFile) data.append("smallImage", smallImageFile);
    if (largeImageFile) data.append("largeImage", largeImageFile);

    const url = isEdit
      ? `http://localhost:8081/api/movies/${movieId}`
      : "http://localhost:8081/api/movies";

    try {
      await axios.post(url, data, {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data",
        },
      });

      notification.success({
        message: isEdit ? "Cập nhật thành công" : "Thêm thành công",
        description: isEdit ? "Phim đã được cập nhật" : "Phim mới đã được thêm",
      });

      onSuccess?.();
      navigate("/admin/movies");
    } catch (err) {
      const msg = err.response?.data?.message || "Lỗi khi gửi dữ liệu phim.";

      if (msg.includes("Tên phim (VN) đã tồn tại")) {
        form.setFields([{ name: "nameVN", errors: ["Tên phim (VN) đã tồn tại"] }]);
      } else if (msg.includes("Tên phim (EN) đã tồn tại")) {
        form.setFields([{ name: "nameEN", errors: ["Tên phim (EN) đã tồn tại"] }]);
      } else {
        notification.error({ message: "Thất bại", description: msg });
      }
    }
  };

  return (
    <Form
      layout="vertical"
      form={ form }
      onFinish={ handleSubmit }
      style={ {
        background: "#fff",
        borderRadius: 12,
        padding: 24,
        margin: 16,
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
      } }
    >
      <h3 className="text-primary fw-bold" style={ { marginBottom: 24 } }>
        { isEdit ? "Cập nhật phim" : "Thêm phim mới" }
      </h3>

      <Row gutter={ 16 }>
        <Col xs={ 24 } md={ 12 }>
          <Form.Item label="Tên phim (VN)" name="nameVN" rules={ [{ required: true }] }>
            <Input />
          </Form.Item>

          <Form.Item label="Tên phim (EN)" name="nameEN" rules={ [{ required: true }] }>
            <Input />
          </Form.Item>

          <Form.Item
            label="Thời lượng (phút)"
            name="duration"
            rules={ [
              { required: true },
              {
                validator: (_, value) =>
                  value > 0
                    ? Promise.resolve()
                    : Promise.reject("Thời lượng phải lớn hơn 0"),
              },
            ] }
          >
            <Input type="number" />
          </Form.Item>

          <Form.Item
            label="Giới hạn tuổi"
            name="ageLimit"
            rules={ [
              { required: true },
              {
                validator: (_, value) =>
                  value >= 0 && value <= 18
                    ? Promise.resolve()
                    : Promise.reject("Giới hạn tuổi phải từ 0 đến 18"),
              },
            ] }
          >
            <Input type="number" min={ 0 } max={ 18 } />
          </Form.Item>

          <Form.Item
            label="Ngày bắt đầu chiếu"
            name="fromDate"
            rules={ [{ required: true, message: "Vui lòng chọn ngày bắt đầu" }] }
          >
            <DatePicker style={ { width: "100%" } } />
          </Form.Item>

          <Form.Item
            label="Ngày kết thúc chiếu"
            name="toDate"
            dependencies={ ["fromDate"] }
            rules={ [
              { required: true, message: "Vui lòng chọn ngày kết thúc" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const fromDate = getFieldValue("fromDate");
                  if (!value || !fromDate || value.isAfter(fromDate)) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error("Ngày kết thúc phải sau ngày bắt đầu"));
                },
              }),
            ] }
          >
            <DatePicker style={ { width: "100%" } } />
          </Form.Item>
        </Col>

        <Col xs={ 24 } md={ 12 }>
          <Form.Item label="Đạo diễn" name="director" rules={ [{ required: true }] }>
            <Input />
          </Form.Item>

          <Form.Item
            label="Hãng sản xuất"
            name="movieProductionCompany"
            rules={ [{ required: true }] }
          >
            <Input />
          </Form.Item>

          <Form.Item label="Poster (ảnh nhỏ)" required>
            <Upload
              beforeUpload={ (file) => {
                setSmallImageFile(file);
                setPreviewSmallImage(URL.createObjectURL(file));
                return false;
              } }
              showUploadList={ false }
            >
              <Button icon={ <UploadOutlined /> } block>
                Chọn ảnh
              </Button>
            </Upload>
            { previewSmallImage && (
              <img
                src={ previewSmallImage }
                alt="Poster"
                style={ { marginTop: 10, width: "100%" } }
              />
            ) }
          </Form.Item>

          <Form.Item label="Banner (ảnh lớn)" required>
            <Upload
              beforeUpload={ (file) => {
                setLargeImageFile(file);
                setPreviewLargeImage(URL.createObjectURL(file));
                return false;
              } }
              showUploadList={ false }
            >
              <Button icon={ <UploadOutlined /> } block>
                Chọn ảnh
              </Button>
            </Upload>
            { previewLargeImage && (
              <img
                src={ previewLargeImage }
                alt="Banner"
                style={ { marginTop: 10, width: "100%" } }
              />
            ) }
          </Form.Item>

          <Form.Item label="Trailer Link" name="trailerLink" rules={ [{ required: true }] }>
            <Input placeholder="https://youtube.com/..." />
          </Form.Item>
        </Col>
      </Row>

      <Form.Item label="Nội dung phim" name="content" rules={ [{ required: true }] }>
        <Input.TextArea rows={ 4 } />
      </Form.Item>

      <Form.Item
        label="Thể loại"
        name="typeIds"
        rules={ [{ required: true, message: "Chọn ít nhất một thể loại!" }] }
      >
        <Select
          mode="multiple"
          allowClear
          placeholder="Chọn thể loại phim"
          options={ types.map((t) => ({ value: t.id, label: t.name })) }
        />
      </Form.Item>

      <Form.Item>
        <Row gutter={ 12 }>
          <Col xs={ 24 } sm={ 12 }>
            <Button type="primary" htmlType="submit" icon={ <PlusOutlined /> } block>
              { isEdit ? "Cập nhật" : "Thêm phim" }
            </Button>
          </Col>
          <Col xs={ 24 } sm={ 12 }>
            <Button icon={ <CloseOutlined /> } onClick={ () => navigate("/admin/movies") } block>
              Hủy
            </Button>
          </Col>
        </Row>
      </Form.Item>
    </Form>
  );
}

