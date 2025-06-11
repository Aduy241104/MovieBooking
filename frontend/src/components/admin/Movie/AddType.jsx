import React, { useState } from 'react';
import axios from 'axios';
import { Button, Form, Alert } from 'react-bootstrap';
import {Film} from "lucide-react";

function AddType() {
    const [typeName, setTypeName] = useState('');
    const [message, setMessage] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post('http://localhost:8081/api/public/types', { name: typeName });
            setMessage('✅ Thêm thể loại thành công!');
            setTypeName('');
        } catch (error) {
            setMessage('❌ Thêm thất bại: ' + (error.response?.data?.message || 'Lỗi không xác định!'));
        }
    };

    return (
        <div className="container mt-4 ">
            <h3 className='d-flex'> <Film size={28} style={{marginTop:"4px", marginRight:"2px"}}/>Thêm thể loại phim</h3>
            {message && <Alert variant="info">{message}</Alert>}
            <Form onSubmit={handleSubmit}>
                <Form.Group controlId="typeName">
                    <Form.Control
                        type="text"
                        value={typeName}
                        onChange={(e) => setTypeName(e.target.value)}
                        placeholder="Nhập tên thể loại"
                        required
                    />
                </Form.Group>
                <Button variant="danger" type="submit" className="mt-3">
                    Thêm
                </Button>
            </Form>
        </div>
    );
}

export default AddType;
