package com.campuslostfound.service;

import com.campuslostfound.dto.item.ItemRequest;
import com.campuslostfound.dto.item.ItemResponse;
import com.campuslostfound.entity.ItemStatus;
import com.campuslostfound.entity.LostItem;
import com.campuslostfound.exception.ResourceNotFoundException;
import com.campuslostfound.repository.LostItemRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LostItemService {

    private final LostItemRepository lostItemRepository;

    public ItemResponse create(ItemRequest request, Long userId) {
        LostItem item = lostItemRepository.save(LostItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .dateLost(request.getDate())
                .imageUrl(request.getImageUrl())
                .createdAt(LocalDateTime.now())
                .status(ItemStatus.OPEN)
                .userId(userId)
                .build());
        return map(item);
    }

    public List<ItemResponse> getAll(String query) {
        List<LostItem> items = lostItemRepository.findAll().stream()
                .filter(item -> item.getStatus() != ItemStatus.CLAIMED && item.getStatus() != ItemStatus.CLOSED)
                .toList();
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
        return map(lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found")));
    }

    public void markMatched(Long id) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));
        item.setStatus(ItemStatus.MATCHED);
        lostItemRepository.save(item);
    }

    public LostItem getEntityById(Long id) {
        return lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));
    }

    public void markClaimed(Long id) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));
        item.setStatus(ItemStatus.CLAIMED);
        lostItemRepository.save(item);
    }

    public void handOverToClaimant(Long id, Long claimantId) {
        LostItem item = lostItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lost item not found"));
        item.setStatus(ItemStatus.CLAIMED);
        item.setUserId(claimantId);
        lostItemRepository.save(item);
    }

    private ItemResponse map(LostItem item) {
        return ItemResponse.builder()
                .id(item.getId())
                .type("lost")
                .title(item.getTitle())
                .description(item.getDescription())
                .location(item.getLocation())
                .date(item.getDateLost())
                .imageUrl(item.getImageUrl())
                .status(item.getStatus())
                .userId(item.getUserId())
                .build();
    }
}
