package com.campuslostfound.service;

import com.campuslostfound.dto.item.ItemRequest;
import com.campuslostfound.dto.item.ItemResponse;
import com.campuslostfound.entity.FoundItem;
import com.campuslostfound.entity.ItemStatus;
import com.campuslostfound.exception.ResourceNotFoundException;
import com.campuslostfound.repository.FoundItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FoundItemService {

    private final FoundItemRepository foundItemRepository;

    public ItemResponse create(ItemRequest request, Long userId) {
        FoundItem item = foundItemRepository.save(FoundItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .dateFound(request.getDate())
                .imageUrl(request.getImageUrl())
                .status(ItemStatus.OPEN)
                .userId(userId)
                .build());
        return map(item);
    }

    public List<ItemResponse> getAll(String query) {
        List<FoundItem> items = foundItemRepository.findAll();
        if (query != null && !query.isBlank()) {
            String[] keywords = query.toLowerCase().split("\\s+");
            items = items.stream().filter(item -> {
                String searchable = (item.getTitle() + " " + item.getDescription() + " " + item.getLocation()).toLowerCase();
                for (String word : keywords) {
                    if (searchable.contains(word)) return true;
                }
                return false;
            }).toList();
        }
        return items.stream().map(this::map).toList();
    }

    public ItemResponse getById(Long id) {
        return map(foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found")));
    }

    public void markMatched(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found"));
        item.setStatus(ItemStatus.MATCHED);
        foundItemRepository.save(item);
    }

    public void markClaimed(Long id) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found"));
        item.setStatus(ItemStatus.CLAIMED);
        foundItemRepository.save(item);
    }

    public void handOverToClaimant(Long id, Long claimantId) {
        FoundItem item = foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found"));
        item.setStatus(ItemStatus.CLAIMED);
        item.setUserId(claimantId);
        foundItemRepository.save(item);
    }

    public FoundItem getEntityById(Long id) {
        return foundItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Found item not found"));
    }

    public LocalDateTime getPostedDateTime(FoundItem item) {
        return item.getDateFound() != null ? item.getDateFound().atStartOfDay() : null;
    }

    private ItemResponse map(FoundItem item) {
        return ItemResponse.builder()
                .id(item.getId())
                .type("found")
                .title(item.getTitle())
                .description(item.getDescription())
                .location(item.getLocation())
                .date(item.getDateFound())
                .imageUrl(item.getImageUrl())
                .status(item.getStatus())
                .userId(item.getUserId())
                .build();
    }
}
