import { LoadingOutlined, UploadOutlined, UserOutlined } from "@ant-design/icons";
import { Avatar, Button, Divider, Form, Input, message, Popconfirm, Tag, Spin, Row, Col, Select, DatePicker } from "antd";
import { Bolt, Cake, Calendar, IdCard, LetterText, Lock, LockOpen, Mail, Phone, UserPen, VenusAndMars } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useOutletContext, useParams } from "react-router-dom";
import dayjs from "dayjs";
import { fetchAccountByIdAPI, updateAccountInfoAPI, updateAccountStatusAPI } from "../../service/AccountService";


export const UserDetailPage = (props) => {
    const { setBreadcrumbItems } = useOutletContext();
    const { userText } = props;

    const [dataUser, setDataUser] = useState("");
    const [loading, setLoading] = useState(true);
    const [isEditing, setIsEditing] = useState(false);
    const { accountId } = useParams();
    const [form] = Form.useForm();

    const menuItems = [
        { key: '1', label: <Link to={"/admin/users-members"}>Thành viên</Link> },
        { key: '2', label: <Link to={"/admin/users-employees"}>Nhân viên</Link> },
    ];

    useEffect(() => {
        setBreadcrumbItems([
            { title: 'Trang chủ', href: '/admin' },
            { title: 'Người dùng', menu: { items: menuItems } },
            { title: userText, href: '/admin' + (userText === "Thành viên" ? '/users-members' : '/users-employees') },
            { title: dataUser?.fullName || (loading ? "Đang tải..." : `Chi tiết ${userText.toLowerCase()}`) },
        ]);
    }, [setBreadcrumbItems, dataUser, loading]);

    useEffect(() => {
        const fetchDataUser = async () => {
            // console.log('Fetching user data for accountId:', accountId);
            if (accountId) {
                setLoading(true);
                const res = await fetchAccountByIdAPI(accountId);
                if (res && res.result) {
                    setDataUser(res.result);
                } else {
                    setDataUser({});
                    message.error(res?.error?.message || "Không thể tải thông tin người dùng.");
                }
                setLoading(false);
            }
        };
        fetchDataUser();
    }, [accountId]);

    const handleUpdateAccountStatus = async (dataUser) => {
        const res = await updateAccountStatusAPI(dataUser.accountId, dataUser.status === 1 ? 0 : 1);
        if (res && res.result) {
            setDataUser(prevUser => ({ ...prevUser, status: dataUser.status === 1 ? 0 : 1 }));
            message.success(
                <span>
                    {dataUser.status === 0 ? 'Mở khoá tài khoản ' : 'Khoá tài khoản '}
                    <span className='font-medium'>{dataUser.email}</span>
                    {' thành công'}
                </span>
            );
        } else {
            message.error(`Lỗi: ${res?.error?.message || 'Cập nhật trạng thái thất bại'}`);
        }
    };

    const handleFormUpdate = async (values) => {
        console.log('>>> ', values)
        const dataToUpdate = {
            ...values,
            dateOfBirth: values.dateOfBirth ? dayjs(values.dateOfBirth).format("YYYY-MM-DD") : null,
        };
        setLoading(true);
        const res = await updateAccountInfoAPI(accountId, dataToUpdate);
        setLoading(false);
        if (res && res.result) {
            message.success("Cập nhật thông tin thành công!");
            setDataUser(res.result);
            setIsEditing(false);
        } else {
            message.error(res?.error?.message || "Cập nhật thông tin thất bại.");
        }
        setIsEditing(false);
    }

    // if (!dataUser || Object.keys(dataUser).length === 0) {
    //     return <div className="text-center mt-10">Không tìm thấy thông tin thành viên hoặc có lỗi xảy ra.</div>;
    // }

    // Chỉ khi isEditing là true, ta mới thực sự cần initialValues cho form edit
    // Khi isEditing là false, các trường sẽ disabled và hiển thị từ dataUser
    const initialFormValues = {
        email: dataUser.email,
        gender: dataUser.gender,
        dateOfBirth: dataUser.dateOfBirth ? dayjs(dataUser.dateOfBirth) : null,
        phoneNumber: dataUser.phoneNumber,
        identityCard: dataUser.identityCard,
        fullName: dataUser.fullName,
        score: dataUser.score == null ? 0 : dataUser.score,
    };


    return (
        <>
            {loading ? (
                <>
                    <div className='flex flex-col justify-center items-center gap-3 h-screen'>
                        <Spin indicator={<LoadingOutlined spin />} size="large" />
                        <span className='text-xl font-semibold'>Đang tải dữ liệu...</span>
                    </div>
                </>
            ) : (
                <>
                    <div className="flex flex-col md:flex-row items-start md:items-center gap-20 px-4">
                        <div className="flex items-center gap-3 w-full md:w-auto">
                            <Avatar size={90} src={dataUser.avatar} icon={!dataUser.avatar && <UserOutlined />} />
                            <div className="flex flex-col gap-1">
                                <p className="text-lg font-medium">{dataUser.fullName}</p>
                                <p className="text-base text-cyan-600 font-medium">{userText}</p>
                                <Button icon={<UploadOutlined />} size={"small"}>Đổi ảnh</Button>
                            </div>
                        </div>

                        <div className="flex-1 w-full mt-3 md:mt-0">
                            <div className="flex items-center justify-between mb-2">
                                <p className="text-lg font-semibold text-gray-500">Thông tin {userText.toLowerCase()}</p>
                                <div className="flex gap-4 items-center">
                                    {!isEditing && (
                                        <Button icon={<UserPen strokeWidth={1.75} />} onClick={() => setIsEditing(true)}
                                            style={{ padding: "18px 12px" }}
                                        >
                                            Chỉnh sửa
                                        </Button>
                                    )}
                                    {typeof dataUser.status === 'number' && (
                                        <Popconfirm
                                            placement="leftTop"
                                            title={dataUser.status === 0 ? "Mở khoá tài khoản" : "Khoá tài khoản"}
                                            description={`Xác nhận ${dataUser.status === 0 ? 'mở khoá' : 'khoá'}?`}
                                            onConfirm={() => handleUpdateAccountStatus(dataUser)}
                                            okText="Xác nhận"
                                            cancelText="Huỷ"
                                        >
                                            <Button danger={dataUser.status === 1} type={dataUser.status === 0 ? "default" : "primary"}
                                                style={{ padding: "18px 12px" }}
                                            >
                                                {dataUser.status === 0 ? <LockOpen strokeWidth={1.75} color="green" /> : <Lock strokeWidth={1.75} />}
                                                {dataUser.status === 0 ? " Mở khoá" : " Khoá TK"}
                                            </Button>
                                        </Popconfirm>
                                    )}
                                </div>
                            </div>
                            <Divider className="mt-0 mb-4" style={{ borderTop: '2px solid #e0e0e0' }} />

                            <Form
                                form={form}
                                layout="vertical"
                                initialValues={initialFormValues}
                                onFinish={handleFormUpdate}
                            >
                                <Row gutter={50}>
                                    <Col xs={20} md={10}>
                                        <Form.Item
                                            label="Họ và tên"
                                            name="fullName"
                                            rules={[
                                                { required: true, message: 'Vui lòng nhập họ tên!' },
                                                { min: 2, message: 'Họ tên phải có ít nhất 2 ký tự!' },
                                                { max: 50, message: 'Họ tên không được quá 50 ký tự!' },
                                                {
                                                    pattern: /^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂẾưăạảấầẩẫậắằẳẵặẹẻẽềềểếỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ\s]+$/,
                                                    message: 'Họ tên chỉ được chứa chữ cái và khoảng trắng!'
                                                }
                                            ]}
                                        >
                                            <Input
                                                prefix={<LetterText size={20} strokeWidth={1.5} />}
                                                placeholder="Nhập họ và tên"
                                                disabled={!isEditing}
                                            />
                                        </Form.Item>

                                        <Form.Item
                                            label="Email"
                                            name="email"
                                            rules={[
                                                { required: true, message: 'Vui lòng nhập email!' },
                                                { type: 'email', message: 'Địa chỉ email không hợp lệ!' },
                                                { max: 100, message: 'Email không được quá 100 ký tự!' }
                                            ]}
                                        >
                                            <Input
                                                prefix={<Mail size={20} strokeWidth={1.5} />}
                                                placeholder="Nhập địa chỉ email"
                                                disabled
                                            />
                                        </Form.Item>

                                        <Form.Item
                                            label="Giới tính"
                                            name="gender"
                                            rules={[{ required: true, message: 'Vui lòng chọn giới tính!' }]}
                                        >
                                            <Select
                                                prefix={<VenusAndMars size={20} strokeWidth={1.5} />}
                                                placeholder="Chọn giới tính"
                                                disabled={!isEditing}
                                            // allowClear
                                            >
                                                <Select.Option value="Nam">Nam</Select.Option>
                                                <Select.Option value="Nữ">Nữ</Select.Option>
                                                <Select.Option value="Khác">Khác</Select.Option>
                                            </Select>
                                        </Form.Item>

                                        <Form.Item
                                            label="Sinh nhật"
                                            name="dateOfBirth"
                                            rules={[
                                                {
                                                    validator: (_, value) => {
                                                        if (!value) return Promise.resolve();
                                                        const today = dayjs();
                                                        const age = today.diff(value, 'year');
                                                        if (age < 16) {
                                                            return Promise.reject(new Error('Tuổi phải từ 16 trở lên!'));
                                                        }
                                                        if (age > 100) {
                                                            return Promise.reject(new Error('Tuổi không được quá 100!'));
                                                        }
                                                        return Promise.resolve();
                                                    }
                                                }
                                            ]}
                                        >
                                            <DatePicker
                                                prefix={<Cake size={20} strokeWidth={1.5} />}
                                                style={{ width: '100%' }}
                                                format="DD/MM/YYYY"
                                                disabled={!isEditing}
                                                disabledDate={(current) => {
                                                    // Không cho chọn ngày trong tương lai và quá 80 năm trước
                                                    const today = dayjs();
                                                    const eightyYearsAgo = today.subtract(80, 'year');
                                                    return current && (current > today || current < eightyYearsAgo);
                                                }}
                                                placeholder="Chọn ngày sinh"
                                            />
                                        </Form.Item>


                                        <Form.Item label="Ngày tạo">
                                            <Input
                                                prefix={<Calendar size={20} strokeWidth={1.5} />}
                                                value={dataUser.registerDate ? dayjs(dataUser.registerDate).format("DD/MM/YYYY hh:mm:ss A") : ""}
                                                disabled
                                            />
                                        </Form.Item>
                                    </Col>

                                    <Col xs={20} md={10}>
                                        <Form.Item
                                            label="Số điện thoại"
                                            name="phoneNumber"
                                            rules={[
                                                {
                                                    pattern: /^0[0-9]{9}$/,
                                                    message: 'Số điện thoại phải có 10 chữ số!'
                                                },
                                                {
                                                    validator: (_, value) => {
                                                        if (!value) return Promise.resolve();
                                                        // Kiểm tra số điện thoại Việt Nam
                                                        const phoneRegex = /^0(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$/;
                                                        if (!phoneRegex.test(value)) {
                                                            return Promise.reject(new Error('Số điện thoại không đúng định dạng Việt Nam!'));
                                                        }
                                                        return Promise.resolve();
                                                    }
                                                }
                                            ]}
                                        >
                                            <Input
                                                addonBefore={
                                                    <div className="flex items-center gap-1">
                                                        <Phone size={20} strokeWidth={1.5} /> +84
                                                    </div>
                                                }
                                                placeholder="Nhập số điện thoại"
                                                disabled={!isEditing}
                                                maxLength={10}
                                            />
                                        </Form.Item>

                                        <Form.Item
                                            label="CCCD"
                                            name="identityCard"
                                            rules={[
                                                {
                                                    pattern: /^[0-9]{12}$/,
                                                    message: 'CCCD phải có đúng 12 chữ số!'
                                                },
                                                {
                                                    validator: (_, value) => {
                                                        if (!value) return Promise.resolve();
                                                        // Kiểm tra CCCD không được toàn số giống nhau
                                                        if (/^(\d)\1{11}$/.test(value)) {
                                                            return Promise.reject(new Error('CCCD không hợp lệ!'));
                                                        }
                                                        return Promise.resolve();
                                                    }
                                                }
                                            ]}
                                        >
                                            <Input
                                                prefix={<IdCard size={20} strokeWidth={1.5} />}
                                                placeholder="Nhập số CCCD (12 chữ số)"
                                                disabled={!isEditing}
                                                maxLength={12}
                                            />
                                        </Form.Item>

                                        <Form.Item
                                            label="Điểm tích luỹ"
                                            name="score"
                                            rules={[
                                                {
                                                    validator: (_, value) => {
                                                        if (value !== undefined && value !== null) {
                                                            const num = Number(value);
                                                            if (isNaN(num)) {
                                                                return Promise.reject(new Error('Điểm phải là số!'));
                                                            }
                                                            if (num < 0) {
                                                                return Promise.reject(new Error('Điểm không được âm!'));
                                                            }
                                                            if (num > 999999) {
                                                                return Promise.reject(new Error('Điểm không được quá 999,999!'));
                                                            }
                                                        }
                                                        return Promise.resolve();
                                                    }
                                                }
                                            ]}
                                        >
                                            <Input
                                                prefix={<Bolt size={20} strokeWidth={1.5} />}
                                                placeholder="Nhập điểm tích lũy"
                                                disabled={!isEditing}
                                                type="number"
                                                min={0}
                                                max={9999999}
                                            />
                                        </Form.Item>

                                        <Form.Item label="Trạng thái">
                                            <Tag color={dataUser.status === 0 ? "volcano" : "green"}>
                                                {dataUser.status === 0 ? "Đã khoá" : "Hoạt động"}
                                            </Tag>
                                        </Form.Item>

                                        {isEditing && (
                                            <Form.Item>
                                                <Button type="primary" htmlType="submit" loading={loading} className="me-2">
                                                    Lưu thay đổi
                                                </Button>
                                                <Button onClick={() => {
                                                    setIsEditing(false);
                                                    form.resetFields();
                                                }}>
                                                    Huỷ
                                                </Button>
                                            </Form.Item>
                                        )}
                                    </Col>
                                </Row>

                            </Form>
                        </div>
                    </div>
                </>
            )}

        </>
    );
}