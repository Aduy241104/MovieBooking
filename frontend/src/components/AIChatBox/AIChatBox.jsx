import { useState, useRef, useEffect } from "react";
import { Input, Button, List, Spin, Typography, Tooltip } from "antd";
import { SendOutlined, RobotOutlined, UserOutlined, LoadingOutlined, CloseOutlined } from "@ant-design/icons";
import styles from "./AIChatBox.module.scss";
import classNames from "classnames/bind";

const cx = classNames.bind(styles);
const { Text } = Typography;

export const AIChatBox = () => {
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const listRef = useRef(null);

    const handleSend = async () => {
        if (!input.trim()) return;
        setMessages(msgs => [...msgs, { role: "user", content: input }]);
        setLoading(true);
        try {
            const res = await fetch("http://localhost:8081/api/public/gemini-chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ question: input }),
            });
            const data = await res.json();
            setMessages(msgs => [...msgs, { role: "ai", content: data.answer }]);
        } catch (e) {
            setMessages(msgs => [...msgs, { role: "ai", content: "Xin lỗi, hệ thống đang bận." }]);
        }
        setInput("");
        setLoading(false);
    };

    useEffect(() => {
        if (listRef.current) {
            listRef.current.scrollTop = listRef.current.scrollHeight;
        }
    }, [messages, open]);

    // Ẩn/hiện chatbox
    if (!open) {
        return (
            <Tooltip title="Chat với AI hỗ trợ đặt vé, hỏi đáp phim...">
                <Button
                    className={cx("chatbox-fab")}
                    type="primary"
                    shape="circle"
                    size="large"
                    icon={<RobotOutlined style={{ fontSize: 22 }} />}
                    onClick={() => setOpen(true)}
                />
            </Tooltip>
        );
    }

    return (
        <div className={cx("chatbox-container")}>
            <div className={cx("chatbox-header")}>
                <div className="d-flex align-items-center gap-2">
                    <img src="https://cdn-icons-png.flaticon.com/512/18355/18355222.png" style={{ width: 32, height: 32 }} alt="" />
                    <span>Chatbot AI hỗ trợ</span>
                </div>
                <Button
                    type="text"
                    icon={<CloseOutlined />}
                    className={cx("chatbox-close")}
                    onClick={() => setOpen(false)}
                />
            </div>
            <div className={cx("chatbox-body")} ref={listRef}>
                <List
                    dataSource={messages}
                    renderItem={item => (
                        <List.Item
                            className={cx("chatbox-message", item.role === "user" ? "user" : "ai")}
                        >
                            <div className={cx("avatar")}>
                                {item.role === "user" ? (
                                    <UserOutlined style={{ color: "#1890ff" }} />
                                ) : (
                                    <RobotOutlined style={{ color: "#52c41a" }} />
                                )}
                            </div>
                            <div className={cx("bubble")}>
                                <Text style={{ color: item.role === "user" ? "#fff" : "#fff" }}>{item.content}</Text>
                            </div>
                        </List.Item>
                    )}
                />
                {loading && (
                    <div className={cx("chatbox-message", "ai")}>
                        <div className={cx("avatar")}>
                            <RobotOutlined style={{ color: "#52c41a" }} />
                        </div>
                        <div className={cx("bubble")}>
                            <Spin indicator={<LoadingOutlined />} size="small" /> <span>Đang trả lời...</span>
                        </div>
                    </div>
                )}
            </div>
            <div className={cx("chatbox-footer")}>
                <Input
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    onPressEnter={handleSend}
                    disabled={loading}
                    placeholder="Nhập câu hỏi về phim, đặt vé..."
                    className={cx("chatbox-input")}
                    autoFocus
                />
                <Button
                    type="primary"
                    icon={<SendOutlined />}
                    onClick={handleSend}
                    loading={loading}
                    disabled={!input.trim()}
                    className={cx("chatbox-send")}
                />
            </div>
        </div>
    );
};