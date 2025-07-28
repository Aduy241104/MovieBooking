import { useState, useRef, useEffect } from "react";
import Markdown from "react-markdown";
import { Send, MessageCircle, X, Bot, User } from "lucide-react";
import { v4 as uuidv4 } from "uuid";

function getOrCreateSessionId() {
    let sessionId = localStorage.getItem("moviebot_session_id");
    if (!sessionId) {
        sessionId = uuidv4();
        localStorage.setItem("moviebot_session_id", sessionId);
    }
    return sessionId;
}

export const AIChatBox = () => {
    const sessionId = getOrCreateSessionId();
    const [messages, setMessages] = useState([
        {
            role: "assistant",
            content: "Xin chào! Tôi là MovieBot Tôi có thể giúp gì cho bạn hôm nay?",
        },
    ]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const [open, setOpen] = useState(false);
    const messagesRef = useRef(null);
    const aiMsgRef = useRef("");

    const chatbotIcon = "https://cdn-icons-png.flaticon.com/512/10817/10817417.png";

    // Xử lý stream response từ API
    async function handleStreamResponse(res) {
        const reader = res.body.getReader();
        const decoder = new TextDecoder();
        let buffer = "";
        aiMsgRef.current = "";
        setMessages((msgs) => [...msgs, { role: "assistant", content: "" }]);

        while (true) {
            const { value, done } = await reader.read();
            if (done) break;
            buffer += decoder.decode(value, { stream: true });
            let lines = buffer.split("\n");
            buffer = lines.pop();

            // console.log("Received lines:", lines);

            for (const line of lines) {
                const jsonStr = line.trim();
                if (jsonStr && jsonStr !== "[DONE]") {
                    try {
                        const json = JSON.parse(jsonStr);
                        const content = json.message?.content;
                        if (content) {
                            aiMsgRef.current += content;
                            setMessages((msgs) => {
                                const last = msgs[msgs.length - 1];
                                if (last && last.role === "assistant") {
                                    return [...msgs.slice(0, -1), { ...last, content: aiMsgRef.current }];
                                }
                                return msgs;
                            });
                        }
                    } catch {}
                }
            }
        }
    }

    // Gửi message
    const handleSend = async () => {
        if (!input.trim()) return;

        setMessages((msgs) => [...msgs, { role: "user", content: input }]);
        setInput("");
        setLoading(true);

        try {
            const res = await fetch("http://localhost:8081/api/public/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    sessionId,
                    messages: [{ role: "user", content: input }],
                    stream: true,
                }),
            });

            if (res.body && window.ReadableStream) {
                await handleStreamResponse(res);
            } else {
                const data = await res.json();
                const aiMsg = data.result?.choices?.[0]?.message?.content || "Không có phản hồi từ AI.";
                setMessages((msgs) => [...msgs, { role: "assistant", content: aiMsg }]);
            }
        } catch (e) {
            setMessages((msgs) => [...msgs, { role: "assistant", content: "Xin lỗi, hệ thống đang bận." }]);
        }

        setLoading(false);
    };

    const quickReplies = [
        // { icon: "🎬", text: "Phim chiếu" },
        { icon: "📅", text: "Lịch chiếu phim hôm nay" },
        { icon: "🔥", text: "Phim Hot" },
        { icon: "🎫", text: "Đặt vé" },
        { icon: "💸", text: "Ưu đãi" },
        { icon: "🔑", text: "Quên mật khẩu" },
        { icon: "✏️", text: "Đăng ký tài khoản" },
        // { icon: "👤", text: "Cập nhật thông tin cá nhân" },
    ];

    // Quick reply handler
    const handleQuickReply = async (item) => {
        setMessages((msgs) => [...msgs, { role: "user", content: item.text }]);
        setLoading(true);

        try {
            const res = await fetch("http://localhost:8081/api/public/chatbot", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    sessionId,
                    messages: [{ role: "user", content: item.text }],
                    stream: true,
                }),
            });
            if (res.body && window.ReadableStream) {
                await handleStreamResponse(res);
            } else {
                const data = await res.json();
                const aiMsg = data.result?.choices?.[0]?.message?.content || "Không có phản hồi từ AI.";
                setMessages((msgs) => [...msgs, { role: "assistant", content: aiMsg }]);
            }
        } catch (e) {
            setMessages((msgs) => [...msgs, { role: "assistant", content: "Xin lỗi, hệ thống đang bận." }]);
        }
        setLoading(false);
    };

    // Auto scroll to bottom
    useEffect(() => {
        if (messagesRef.current) {
            messagesRef.current.scrollTop = messagesRef.current.scrollHeight;
        }
    }, [messages]);

    // FAB Button khi chatbox đóng
    if (!open) {
        return (
            <div className="fixed bottom-19 right-5 z-50">
                <button
                    onClick={() => setOpen(true)}
                    className="relative w-12 h-12 bg-blue-500 hover:bg-blue-600 shadow-lg flex items-center justify-center group hover:scale-105 transition-all overflow-hidden"
                    style={{ borderRadius: "50%" }}
                >
                    <MessageCircle className="w-6 h-6 text-white" />
                    <div className="absolute bottom-16 right-0 bg-gray-900 text-white px-3 py-2 rounded-lg text-sm opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap shadow-lg">
                        Chat với MovieBot AI
                        <div className="absolute -bottom-1 right-6 w-2 h-2 bg-gray-900 rotate-45"></div>
                    </div>
                </button>
            </div>
        );
    }

    return (
        <div className="fixed bottom-4 right-4 z-999 w-[350px] max-w-[95vw] h-[520px] max-h-[80vh] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden">
            {/* Header */}
            <div className="bg-white px-3 py-2 flex items-center justify-between border-b border-gray-100">
                <div className="flex items-center space-x-2">
                    <div className="w-9 h-9 bg-blue-500 rounded-full flex items-center justify-center">
                        <MessageCircle className="w-5 h-5 text-white" />
                    </div>
                    <div>
                        <p className="text-gray-900 font-semibold text-xl">MovieBot</p>
                        <div className="flex items-center text-xs text-gray-500">
                            <span className="w-1.5 h-1.5 bg-green-400 rounded-full mr-1.5"></span>
                            Online
                        </div>
                    </div>
                </div>
                <button
                    onClick={() => setOpen(false)}
                    className="w-7 h-7 flex items-center justify-center hover:bg-gray-100 !rounded-full transition-colors"
                >
                    <X className="w-5 h-5 text-gray-400" />
                </button>
            </div>
            {/* Messages */}
            <div ref={messagesRef} className="flex-1 px-3 py-4 space-y-4 overflow-y-auto bg-gray-100 min-h-0">
                {messages.map((message, index) => (
                    <div key={index} className={`flex ${message.role === "user" ? "justify-end" : "justify-start"}`}>
                        <div
                            className={`flex items-start space-x-2 max-w-[90%] ${
                                message.role === "user" ? "flex-row-reverse space-x-reverse" : ""
                            }`}
                        >
                            <div
                                className={`w-7 h-7 rounded-full flex items-center justify-center flex-shrink-0 mt-1 ${
                                    message.role === "user" ? "bg-gray-400" : "bg-cyan-500"
                                }`}
                            >
                                {message.role === "user" ? (
                                    <User className="w-4 h-4 text-white" />
                                ) : (
                                    <img src={chatbotIcon} alt="" className="w-7 h-7" />
                                )}
                            </div>
                            <div
                                className={`px-3 py-2 rounded-2xl ${
                                    message.role === "user"
                                        ? "bg-blue-500 text-white"
                                        : "bg-white text-gray-800 shadow-sm"
                                }`}
                            >
                                {message.role === "assistant" ? (
                                    <div className="text-sm">
                                        <Markdown>{message.content}</Markdown>
                                    </div>
                                ) : (
                                    <p className="text-sm">{message.content}</p>
                                )}
                            </div>
                        </div>
                    </div>
                ))}

                {/* Quick replies trong chat */}
                {messages.length === 1 && !loading && (
                    <div className="flex justify-start">
                        <div className="flex items-start space-x-2 max-w-[90%]">
                            <div className="w-7 h-7 bg-cyan-500 rounded-full flex items-center justify-center flex-shrink-0 mt-1">
                                <img src={chatbotIcon} alt="" className="w-7 h-7" />
                            </div>
                            <div className="bg-white px-3 py-2 rounded-2xl shadow-sm">
                                <p className="text-sm text-gray-700 mb-3">
                                    Tôi có thể giúp bạn tìm hiểu về thông tin phim, lịch chiếu, đặt vé và nhiều thông
                                    tin khác!
                                </p>
                                <div className="flex flex-col items-start gap-2">
                                    {quickReplies.map((item, index) => (
                                        <button
                                            key={index}
                                            onClick={() => handleQuickReply(item)}
                                            disabled={loading}
                                            className="px-[10px] py-1.5 hover:bg-gray-100 !rounded-full !text-[12px] font-medium transition-colors border border-gray-400 shadow-sm"
                                        >
                                            {item.icon} {item.text}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Loading state */}
                {loading && (
                    <div className="flex justify-start">
                        <div className="flex items-start space-x-2 max-w-[80%]">
                            <div className="w-7 h-7 bg-blue-500 rounded-full flex items-center justify-center mt-1">
                                <Bot className="w-4 h-4 text-white" />
                            </div>
                            <div className="bg-white px-3 py-2 rounded-2xl shadow-sm">
                                <div className="flex space-x-1">
                                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                                    <div
                                        className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                                        style={{ animationDelay: "0.1s" }}
                                    ></div>
                                    <div
                                        className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                                        style={{ animationDelay: "0.2s" }}
                                    ></div>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </div>
            {/* Input */}
            <div className="p-3 border-t border-gray-100 bg-white">
                <div className="flex items-center space-x-2">
                    <input
                        type="text"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => e.key === "Enter" && !loading && handleSend()}
                        placeholder="Nhập tin nhắn..."
                        disabled={loading}
                        className="flex-1 px-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-1 focus:ring-slate-300 focus:border-transparent disabled:opacity-50 text-sm bg-gray-50"
                    />
                    <button
                        onClick={handleSend}
                        disabled={loading || !input.trim()}
                        className="w-9 h-9 flex items-center justify-center transition-colors disabled:opacity-50"
                        style={{ background: "none", boxShadow: "none" }}
                    >
                        {loading ? (
                            <div className="w-4 h-4 border-2 border-slate-300 border-t-transparent rounded-full animate-spin"></div>
                        ) : (
                            <Send className="w-6 h-6 text-blue-500" />
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};
