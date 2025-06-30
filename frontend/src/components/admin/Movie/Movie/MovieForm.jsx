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
import { PlusOutlined, UploadOutlined, CloseOutlined } from "@ant-design/icons";
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

  useEffect(() => {
    axios.get("http://localhost:8081/api/public/types").then((res) => {
      setTypes(res.data);
    });

    if (isEdit) {
      axios.get(`http://localhost:8081/api/public/movies/${movieId}`).then((r) => {
        const m = r.data;
        form.setFieldsValue({
          nameVN: m.nameVN,
          nameEN: m.nameEN,
          duration: m.duration,
          content: m.content,
          fromDate: moment(m.fromDate),
          toDate: moment(m.toDate),
          director: m.director,
          actor: m.actor,
          movieProductionCompany: m.movieProductionCompany,
          ageLimit: m.ageLimit,
          trailerLink: m.trailerUrl || '',
          typeIds: m.typeIds || []
        });

        const BASE = "http://localhost:8081";
        setPreviewSmallImage(m.smallImageUrl ? encodeURI(`${BASE}${m.smallImageUrl}`) : null);
        setPreviewLargeImage(m.largeImageUrl ? encodeURI(`${BASE}${m.largeImageUrl}`) : null);
      });
    }
  }, [form, movieId, isEdit]);
  const normalizeYouTubeUrl = (url) => {
    if (!url) return '';
    try {
      const videoId =
        url.includes('youtu.be/')
          ? url.split('youtu.be/')[1]
          : url.includes('watch?v=')
            ? url.split('watch?v=')[1].split('&')[0]
            : null;

      return videoId ? `https://www.youtube.com/embed/${videoId}` : url;
    } catch {
      return url;
    }
  };

  const handleSubmit = async (values) => {
    const data = new FormData();


    const normalizedValues = {
      ...values,
      trailerLink: normalizeYouTubeUrl(values.trailerLink),
    };

    Object.entries(normalizedValues).forEach(([key, value]) => {
      if (key === "fromDate" || key === "toDate") {
        data.append(key, value.format("YYYY-MM-DD"));
      } else if (key === "typeIds") {
        value.forEach((id) => data.append("typeIds", id));
      } else {
        data.append(key, value);
      }
    });

    if (smallImageFile) data.append("smallImage", smallImageFile);
    if (largeImageFile) data.append("largeImage", largeImageFile);

    const url = isEdit
      ? `http://localhost:8081/api/public/movies/${movieId}`
      : "http://localhost:8081/api/public/movies";

    try {
      await axios.post(url, data, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      notification.success({
        message: isEdit ? "Cập nhật thành công" : "Thêm thành công",
        description: isEdit ? "Phim đã được cập nhật" : "Phim mới đã được thêm",
      });

      if (onSuccess) onSuccess();
      navigate("/admin/movies");
    } catch (err) {
      const msg = err.response?.data?.message || "Lỗi khi gửi dữ liệu phim.";
      notification.error({ message: "Thất bại", description: msg });
    }
  };

  return (
    <Form
      layout="vertical"
      form={form}
      onFinish={handleSubmit}
      style={{ maxWidth: 1000, margin: "auto", padding: 24, background: "#fff", borderRadius: 12, boxShadow: "0 2px 8px rgba(0,0,0,0.1)" }}
    >
      <h3 className="text-primary fw-bold" style={{ marginBottom: 24 }}>
        {isEdit ? "Cập nhật phim" : "Thêm phim mới"}
      </h3>

      <Row gutter={16}>
        <Col span={12}>
          <Form.Item label="Tên phim (VN)" name="nameVN" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item label="Tên phim (EN)" name="nameEN" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {/* <Form.Item label="Thời lượng (phút)" name="duration" rules={[{ required: true }]}>
            <Input type="number" />
          </Form.Item>
          <Form.Item label="Giới hạn tuổi" name="ageLimit" rules={[{ required: true }]}>
            <Input type="number" />
          </Form.Item> */}
          <Form.Item
            label="Thời lượng (phút)"
            name="duration"
            rules={[
              { required: true, message: "Vui lòng nhập thời lượng." },
              {
                validator: (_, value) =>
                  value > 0
                    ? Promise.resolve()
                    : Promise.reject(new Error("Thời lượng phải lớn hơn 0")),
              },
            ]}
          >
            <Input type="number" />
          </Form.Item>

          <Form.Item
            label="Giới hạn tuổi"
            name="ageLimit"
            rules={[
              { required: true, message: "Vui lòng nhập giới hạn tuổi." },
              {
                validator: (_, value) =>
                  value >= 0
                    ? Promise.resolve()
                    : Promise.reject(new Error("Giới hạn tuổi phải lớn hơn hoặc bằng 0")),
              },
            ]}
          >
            <Input type="number" />
          </Form.Item>

          <Form.Item
            label="Ngày bắt đầu chiếu"
            name="fromDate"
            rules={[
              { required: true, message: "Vui lòng chọn ngày bắt đầu" },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  const toDate = getFieldValue("toDate");
                  if (!value || !toDate || value.isBefore(toDate)) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error("Ngày bắt đầu phải trước ngày kết thúc"));
                },
              }),
            ]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            label="Ngày kết thúc chiếu"
            name="toDate"
            rules={[
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
            ]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

        </Col>

        <Col span={12}>
          <Form.Item label="Đạo diễn" name="director" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {/* <Form.Item label="Diễn viên" name="actor" rules={[{ required: true }]}>
            <Input />
          </Form.Item> */}
          <Form.Item label="Hãng sản xuất" name="movieProductionCompany" rules={[{ required: true }]}>
            <Input />
          </Form.Item>

          <Form.Item label="Poster (ảnh nhỏ)" required>
            <Upload
              beforeUpload={(file) => {
                setSmallImageFile(file);
                setPreviewSmallImage(URL.createObjectURL(file));
                return false;
              }}
              showUploadList={false}
            >
              <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
            </Upload>
            {previewSmallImage && (
              <img src={previewSmallImage} alt="Poster" style={{ marginTop: 10, maxHeight: 150 }} />
            )}
          </Form.Item>

          <Form.Item label="Banner (ảnh lớn)" required>
            <Upload
              beforeUpload={(file) => {
                setLargeImageFile(file);
                setPreviewLargeImage(URL.createObjectURL(file));
                return false;
              }}
              showUploadList={false}
            >
              <Button icon={<UploadOutlined />}>Chọn ảnh</Button>
            </Upload>
            {previewLargeImage && (
              <img src={previewLargeImage} alt="Banner" style={{ marginTop: 10, maxHeight: 150 }} />
            )}
          </Form.Item>

          <Form.Item label="Trailer Link" name="trailerLink" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
        </Col>
      </Row>

      <Form.Item label="Nội dung phim" name="content" rules={[{ required: true }]}>
        <Input.TextArea rows={4} />
      </Form.Item>

      <Form.Item label="Thể loại" name="typeIds" rules={[{ required: true, message: "Chọn ít nhất một thể loại!" }]}>
        <Select
          mode="multiple"
          allowClear
          placeholder="Chọn thể loại phim"
          options={types.map((t) => ({ value: t.id, label: t.name }))}
        />
      </Form.Item>

      <Form.Item>
        <Row gutter={12}>
          <Col>
            <Button type="primary" htmlType="submit" icon={<PlusOutlined />}>
              {isEdit ? "Cập nhật" : "Thêm phim"}
            </Button>
          </Col>
          <Col>
            <Button icon={<CloseOutlined />} onClick={() => navigate("/admin/movies")}>
              Hủy
            </Button>
          </Col>
        </Row>
      </Form.Item>
    </Form>
  );
}
