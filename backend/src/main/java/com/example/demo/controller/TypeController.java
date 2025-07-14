//package com.example.demo.controller;
//
//
//import com.example.demo.model.Type;
//import com.example.demo.service.TypeService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/public/types")
//@CrossOrigin(origins = "*") // Cho phép React gọi API
//public class TypeController {
//
//    @Autowired
//    private TypeService typeService;
//
//    @PostMapping
//    public Type addType(@RequestBody Type type) {
//        return typeService.addType(type);
//    }
//    @GetMapping
//    public List<Type> getAllTypes() {
//        return typeService.getAllTypes();
//    }
//}
//
package com.example.demo.controller;

import com.example.demo.model.Type;
import com.example.demo.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/types")
@CrossOrigin(origins = "*")
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

    @PutMapping("/{id}")
    public Type updateType(@PathVariable Integer id, @RequestBody Type updatedType) {
        return typeService.updateType(id, updatedType);
    }

    @DeleteMapping("/{id}")
    public void deleteType(@PathVariable Integer id) {
        typeService.deleteType(id);
    }

    @GetMapping("/{id}")
    public Optional<Type> getTypeById(@PathVariable Integer id) {
        return typeService.getTypeById(id);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .badRequest()
                .body(new java.util.HashMap<String, String>() {{
                    put("message", ex.getMessage());
                }});
    }
}
