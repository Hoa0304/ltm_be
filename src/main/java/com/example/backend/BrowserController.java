package com.example.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class BrowserController {
    private final List<String> urls = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String filePath = "src/main/resources/urls.json";

    public BrowserController() {
        loadUrlsFromFile();
    }

    private void loadUrlsFromFile() {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                List<String> loadedUrls = objectMapper.readValue(file, new TypeReference<List<String>>() {});
                urls.addAll(loadedUrls);
            }
        } catch (IOException e) {
            System.err.println("Error loading URLs from file: " + e.getMessage());
        }
    }

    private void saveUrlsToFile() {
        try {
            objectMapper.writeValue(new File(filePath), urls);
            System.out.println("URLs saved to file successfully.");
        } catch (IOException e) {
            System.err.println("Error saving URLs to file: " + e.getMessage());
        }
    }

    @GetMapping("/fetch")
    public String fetchUrl(@RequestParam String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                return "Invalid URL: must start with http:// or https://";
            }

            Document doc = Jsoup.connect(url).get();

            return doc.html();
        } catch (Exception e) {
            return "Error fetching the URL: " + e.getMessage();
        }
    }

    @PostMapping("/api/urls")
    public ResponseEntity<String> addUrl(@RequestBody UrlRequest urlRequest) {
        System.out.println("Add URL: " + urlRequest.getUrl());
        if (urlRequest.getUrl() == null || urlRequest.getUrl().isEmpty()) {
            return ResponseEntity.badRequest().body("error URL");
        }

        if (!urls.contains(urlRequest.getUrl())) {
            urls.add(urlRequest.getUrl());
            saveUrlsToFile();
            return ResponseEntity.ok("URL added successfully");
        } else {
            return ResponseEntity.status(409).body("URL already exists");
        }
    }

    @GetMapping("/api/urls")
    public List<String> getUrls() {
        System.out.println("List URL: " + urls);
        return urls;
    }
}