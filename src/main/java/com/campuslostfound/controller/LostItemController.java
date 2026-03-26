package com.campuslostfound.controller;

import com.campuslostfound.dto.item.ItemRequest;
import com.campuslostfound.dto.item.ItemResponse;
import com.campuslostfound.dto.item.PosterDetailsResponse;
import com.campuslostfound.service.ClaimRequestService;
import com.campuslostfound.service.LostItemService;
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
@RequestMapping("/api/lost-items")
@RequiredArgsConstructor
public class LostItemController {

    private final LostItemService lostItemService;
    private final ClaimRequestService claimRequestService;
    private final UserLookup userLookup;

    @PostMapping
    public ItemResponse create(@Valid @RequestBody ItemRequest request, Principal principal, HttpServletRequest httpRequest) {
        return lostItemService.create(request, userLookup.getUserId(principal, httpRequest));
    }

    @GetMapping
    public List<ItemResponse> getAll(@RequestParam(name = "query", required = false) String query) {
        return lostItemService.getAll(query);
    }

    @GetMapping("/{id}")
    public ItemResponse getById(@PathVariable("id") Long id) {
        return lostItemService.getById(id);
    }

    @GetMapping("/{id}/poster")
    public PosterDetailsResponse getPoster(@PathVariable("id") Long id) {
        return claimRequestService.getPosterDetails(id, "lost");
    }
}
