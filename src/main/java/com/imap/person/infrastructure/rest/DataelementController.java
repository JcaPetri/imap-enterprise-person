package com.imap.person.infrastructure.rest;

import com.imap.person.application.service.DataelementService;
import com.imap.person.infrastructure.entity.PerDataelementEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/dataelements")
public class DataelementController {

    private final DataelementService service;

    public DataelementController(DataelementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PerDataelementEntity>> findByCategory(
            @RequestParam String category) {
        return ResponseEntity.ok(service.findByCategory(category));
    }
}
