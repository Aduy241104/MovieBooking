package com.example.demo.service;

import com.example.demo.model.Type;
import com.example.demo.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypeService {

    @Autowired
    private TypeRepository typeRepository;

    public Type addType(Type type) {
        if (typeRepository.existsByName(type.getName())) {
            throw new RuntimeException("Thể loại đã tồn tại!");
        }
        return typeRepository.save(type);
    }
    public List<Type> getAllTypes() {
        return typeRepository.findByIsDeletedFalse();
    }

    public Type updateType(Integer id, Type updatedType) {
        Type existing = typeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thể loại"));

        // Kiểm tra tên trùng với thể loại khác
        if (typeRepository.existsByNameAndIdNot(updatedType.getName(), id)) {
            throw new RuntimeException("Tên thể loại đã tồn tại!");
        }

        existing.setName(updatedType.getName());
        return typeRepository.save(existing);
    }

    public void deleteType(Integer id) {
        Optional<Type> typeOpt = typeRepository.findById(id);
        if (typeOpt.isEmpty()) {
            throw new RuntimeException("Không tìm thấy thể loại để xóa!");
        }
        Type type = typeOpt.get();
        type.setIsDeleted(true);
        typeRepository.save(type);
    }

    public Optional<Type> getTypeById(Integer id) {
        return typeRepository.findById(id);
    }
}