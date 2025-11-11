package com.kma.backend.controller;

import com.kma.backend.model.TestItem;
import com.kma.backend.repository.TestItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private TestItemRepository testItemRepository;

    @GetMapping("/api/test-data")
    public List<TestItem> getTestData() {
        return testItemRepository.findAll();
    }
}