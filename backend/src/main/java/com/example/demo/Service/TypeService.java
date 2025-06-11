package com.example.demo.service;

import com.example.demo.model.Type;
import com.example.demo.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        return typeRepository.findAll();
    }

}
