package com.campuslostfound.controller;

import com.campuslostfound.dto.claim.ClaimRequestDto;
import com.campuslostfound.dto.claim.ClaimResponseDto;
import com.campuslostfound.service.ClaimRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimRequestService claimRequestService;
    private final UserLookup userLookup;

    @GetMapping("/incoming")
    public List<ClaimResponseDto> getIncomingClaims(Principal principal, HttpServletRequest httpRequest) {
        return claimRequestService.getClaimsForPoster(userLookup.getUserId(principal, httpRequest));
    }

    @GetMapping("/outgoing")
    public List<ClaimResponseDto> getOutgoingClaims(Principal principal, HttpServletRequest httpRequest) {
        return claimRequestService.getClaimsForClaimant(userLookup.getUserId(principal, httpRequest));
    }

    @PostMapping("/request")
    public ClaimResponseDto createRequest(
            @Valid @RequestBody ClaimRequestDto request,
            Principal principal,
            HttpServletRequest httpRequest
    ) {
        return claimRequestService.createRequest(request, userLookup.getUserId(principal, httpRequest));
    }

    @PatchMapping("/{id}/approve")
    public ClaimResponseDto approveRequest(
            @PathVariable("id") Long id,
            Principal principal,
            HttpServletRequest httpRequest
    ) {
        return claimRequestService.approve(id, userLookup.getUserId(principal, httpRequest));
    }
}
