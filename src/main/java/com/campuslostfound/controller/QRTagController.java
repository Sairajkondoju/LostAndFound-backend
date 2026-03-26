package com.campuslostfound.controller;

import com.campuslostfound.entity.QRTag;
import com.campuslostfound.repository.QRTagRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr-tags")
@RequiredArgsConstructor
public class QRTagController {

    private final QRTagRepository qrTagRepository;

    @PostMapping
    public ResponseEntity<QRTag> generateTag(@RequestBody Map<String, String> payload) {
        Long userId = Long.valueOf(payload.get("userId"));
        String itemName = payload.get("itemName");

        String tagId = "TAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        QRTag saved = qrTagRepository.save(QRTag.builder()
                .tagId(tagId)
                .userId(userId)
                .itemName(itemName)
                .createdAt(LocalDateTime.now())
                .build());

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QRTag>> getUserTags(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(qrTagRepository.findByUserId(userId));
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<QRTag> getTagInfo(@PathVariable("tagId") String tagId) {
        return qrTagRepository.findByTagId(tagId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
