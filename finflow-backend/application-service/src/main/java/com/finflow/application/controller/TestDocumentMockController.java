package com.finflow.application.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
public class TestDocumentMockController {

    @GetMapping("/{id}/exists")
    public boolean documentsExist(@PathVariable UUID id) {
        return true;
    }
}
