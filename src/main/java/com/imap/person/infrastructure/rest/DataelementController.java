package com.imap.person.infrastructure.rest;

import com.imap.person.infrastructure.entity.PerDataelementEntity;
import com.imap.person.infrastructure.repository.PerDataelementJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/dataelements")
public class DataelementController {

    private final PerDataelementJpaRepository repo;

    public DataelementController(PerDataelementJpaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<PerDataelementEntity>> findByCategory(
            @RequestParam String category) {
        return ResponseEntity.ok(repo.findByCategoryAndActiveTrue(category));
    }
}
