package com.example.demo.controller;

import com.example.demo.model.Type;
import com.example.demo.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/types")
@CrossOrigin(origins = "*")
public class TypeController {

    @Autowired
    private TypeService typeService;

    /**
     * Create a new movie type.
     *
     * @param type The type object to create.
     * @return The created type.
     */
    @PostMapping
    public Type addType(@RequestBody Type type) {
        return typeService.addType(type);
    }

    /**
     * Get all movie types that are not marked as deleted.
     *
     * @return A list of active types.
     */
    @GetMapping
    public List<Type> getAllTypes() {
        return typeService.getAllTypes();
    }

    /**
     * Update an existing movie type by its ID.
     *
     * @param id The ID of the type to update.
     * @param updatedType The updated type information.
     * @return The updated type.
     */
    @PutMapping("/{id}")
    public Type updateType(@PathVariable Integer id, @RequestBody Type updatedType) {
        return typeService.updateType(id, updatedType);
    }

    /**
     * Soft delete a type by its ID.
     *
     * @param id The ID of the type to delete.
     */
    @DeleteMapping("/{id}")
    public void deleteType(@PathVariable Integer id) {
        typeService.deleteType(id);
    }

    /**
     * Get a type by its ID.
     *
     * @param id The ID of the type.
     * @return An Optional containing the type, if found.
     */
    @GetMapping("/{id}")
    public Optional<Type> getTypeById(@PathVariable Integer id) {
        return typeService.getTypeById(id);
    }

    /**
     * Global exception handler for runtime exceptions.
     *
     * @param ex The thrown exception.
     * @return A ResponseEntity with a JSON body containing the error message.
     */

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .badRequest()
                .body(new java.util.HashMap<String, String>() {{
                    put("message", ex.getMessage());
                }});
    }
}
