package com.example.demo.controller;


import com.example.demo.model.Type;
import com.example.demo.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/types")
@CrossOrigin(origins = "*") // Cho phép React gọi API
public class TypeController {

    @Autowired
    private TypeService typeService;

    @PostMapping
    public Type addType(@RequestBody Type type) {
        return typeService.addType(type);
    }
    @GetMapping
    public List<Type> getAllTypes() {
        return typeService.getAllTypes();
    }
}

