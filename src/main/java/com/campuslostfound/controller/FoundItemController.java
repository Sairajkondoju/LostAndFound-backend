package com.campuslostfound.controller;

import com.campuslostfound.dto.item.ItemRequest;
import com.campuslostfound.dto.item.ItemResponse;
import com.campuslostfound.dto.item.PosterDetailsResponse;
import com.campuslostfound.service.ClaimRequestService;
import com.campuslostfound.service.FoundItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/found-items")
@RequiredArgsConstructor
public class FoundItemController {

    private final FoundItemService foundItemService;
    private final ClaimRequestService claimRequestService;
    private final UserLookup userLookup;

    @PostMapping
    public ItemResponse create(@Valid @RequestBody ItemRequest request, Principal principal, HttpServletRequest httpRequest) {
        return foundItemService.create(request, userLookup.getUserId(principal, httpRequest));
    }

    @GetMapping
    public List<ItemResponse> getAll(@RequestParam(name = "query", required = false) String query) {
        return foundItemService.getAll(query);
    }

    @GetMapping("/{id}")
    public ItemResponse getById(@PathVariable("id") Long id) {
        return foundItemService.getById(id);
    }

    @GetMapping("/{id}/poster")
    public PosterDetailsResponse getPoster(@PathVariable("id") Long id) {
        return claimRequestService.getPosterDetails(id, "found");
    }
}
