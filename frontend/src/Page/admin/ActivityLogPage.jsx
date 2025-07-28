import { SearchOutlined } from "@ant-design/icons";
import { Input, Select } from "antd";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { ActivityLogTable } from "../../components/admin/Table/ActivityLogTable";
import { CirclePlus, Settings, Trash } from "lucide-react";
import { sfAnd, sfEqual, sfLike, sfLower, sfOr } from "spring-filter-query-builder";
import { debounce } from "lodash";

export const ActivityLogPage = ({ logsText }) => {
    const { setBreadcrumbItems } = useOutletContext();

    const [searchLogs, setSearchLogs] = useState("");
    const [activityFilter, setActivityFilter] = useState("");
    const [filter, setFilter] = useState("");
    const [isInitialized, setIsInitialized] = useState(false);

    // Thiết lập breadcrumb items
    useEffect(() => {
        setBreadcrumbItems([{ title: "Trang chủ", href: "/admin" }, { title: `${logsText}` }]);
        setIsInitialized(true);
    }, []);

    useEffect(() => {
        if (!isInitialized) return;

        const handleFilterActivity = debounce(() => {
            const filters = [];
            if (searchLogs) {
                filters.push(
                    sfOr([
                        sfLike(sfLower("updatedBy"), `*${searchLogs}*`),
                        sfLike(sfLower("userUpdated"), `*${searchLogs}*`),
                        sfLike(sfLower("entityType"), `*${searchLogs}*`),
                    ])
                );
            }
            if (activityFilter !== null && activityFilter !== undefined) {
                filters.push(sfEqual("action", activityFilter));
            }
            if (filters.length === 0) {
                setFilter("");
            } else {
                const filter = sfAnd(filters);
                setFilter(filter.toString());
            }
        }, 400);

        if (searchLogs || activityFilter !== null) {
            handleFilterActivity();
        }

        return () => handleFilterActivity.cancel();
    }, [searchLogs, activityFilter]);

    return (
        <div
            style={{
                padding: 24,
                // minHeight: 360,
                background: "#fff",
                borderRadius: "8px",
                boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
            }}
        >
            <div className="flex justify-between mb-4">
                <div style={{ display: "flex", gap: "2rem" }}>
                    <Input
                        style={{ width: "30vw" }}
                        size="large"
                        addonBefore={<SearchOutlined />}
                        placeholder="Tìm kiếm hoạt động..."
                        allowClear
                        onChange={(value) => setSearchLogs(value.target.value)}
                    />
                    <Select
                        size="large"
                        style={{ width: "10vw" }}
                        options={[
                            {
                                value: "CẬP NHẬT",
                                label: (
                                    <>
                                        <div className="flex items-center gap-1">
                                            <Settings size={20} strokeWidth={1.5} color="#0e4ad8" />
                                            <span style={{ marginLeft: 8 }}>CẬP NHẬT</span>
                                        </div>
                                    </>
                                ),
                            },
                            {
                                value: "TẠO MỚI",
                                label: (
                                    <>
                                        <div className="flex items-center gap-1">
                                            <CirclePlus size={20} strokeWidth={1.5} color="#22a220" />
                                            <span style={{ marginLeft: 8 }}>TẠO MỚI</span>
                                        </div>
                                    </>
                                ),
                            },
                            {
                                value: "XOÁ",
                                label: (
                                    <>
                                        <div className="flex items-center gap-1">
                                            <Trash size={20} strokeWidth={1.5} color="#ce0d0d" />
                                            <span style={{ marginLeft: 8 }}>XOÁ</span>
                                        </div>
                                    </>
                                ),
                            },
                        ]}
                        placeholder="Hành động"
                        allowClear
                        onChange={(value) => setActivityFilter(value)}
                    />
                </div>
            </div>

            <ActivityLogTable filter={filter} />
        </div>
    );
};
