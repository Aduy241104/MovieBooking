import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Button, Table, Spinner } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';

function TypeList() {
    const [types, setTypes] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        fetchTypes();
    }, []);

    const fetchTypes = async () => {
        try {
            const response = await axios.get('http://localhost:8081/api/public/types');
            setTypes(response.data);
        } catch (error) {
            console.error('Lỗi khi tải thể loại:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAddClick = () => {
        navigate('/admin/movie/movie-type/add');
    };

    return (
        <div className="container mt-4 text-white">
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h3>📋 Danh sách thể loại phim</h3>
                <Button variant="danger" onClick={handleAddClick}>
                    ➕ Thêm thể loại
                </Button>
            </div>

            {loading ? (
                <div className="text-center">
                    <Spinner animation="border" variant="light" />
                </div>
            ) : (
                <Table striped bordered hover variant="dark">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Tên thể loại</th>
                        </tr>
                    </thead>
                    <tbody>
                        {types.map((type, index) => (
                            <tr key={type.id}>
                                <td>{index + 1}</td>
                                <td>{type.name}</td>
                            </tr>
                        ))}
                    </tbody>
                </Table>
            )}
        </div>
    );
}

export default TypeList;
