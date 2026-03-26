package com.campuslostfound.controller;

import com.campuslostfound.dto.claim.ClaimResponseDto;
import com.campuslostfound.service.ClaimRequestService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ClaimRequestService claimRequestService;

    @GetMapping("/claims")
    public List<ClaimResponseDto> getClaims() {
        return claimRequestService.getAll();
    }

    @PatchMapping("/claims/{id}/approve")
    public ClaimResponseDto approveClaim(@PathVariable("id") Long id) {
        return claimRequestService.getAll().stream()
                .filter(claim -> claim.getId().equals(id))
                .findFirst()
                .map(claim -> claimRequestService.approve(id, claim.getPosterId()))
                .orElseThrow(() -> new com.campuslostfound.exception.ResourceNotFoundException("Claim request not found"));
    }
}
