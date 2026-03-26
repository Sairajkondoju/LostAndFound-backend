package com.campuslostfound.service;

import com.campuslostfound.dto.claim.ClaimRequestDto;
import com.campuslostfound.dto.claim.ClaimResponseDto;
import com.campuslostfound.dto.item.PosterDetailsResponse;
import com.campuslostfound.entity.ClaimRequest;
import com.campuslostfound.entity.ClaimStatus;
import com.campuslostfound.entity.FoundItem;
import com.campuslostfound.entity.ItemStatus;
import com.campuslostfound.entity.LostItem;
import com.campuslostfound.entity.User;
import com.campuslostfound.exception.BadRequestException;
import com.campuslostfound.exception.ResourceNotFoundException;
import com.campuslostfound.repository.ClaimRequestRepository;
import com.campuslostfound.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClaimRequestService {

    private final ClaimRequestRepository claimRequestRepository;
    private final LostItemService lostItemService;
    private final FoundItemService foundItemService;
    private final UserRepository userRepository;

    public PosterDetailsResponse getPosterDetails(Long itemId, String itemType) {
        boolean isFoundItem = "found".equalsIgnoreCase(itemType);
        Long posterId;
        String message;
        LocalDateTime postedDate;

        if (isFoundItem) {
            FoundItem item = foundItemService.getEntityById(itemId);
            posterId = item.getUserId();
            message = item.getDescription();
            postedDate = foundItemService.getPostedDateTime(item);
        } else {
            LostItem item = lostItemService.getEntityById(itemId);
            posterId = item.getUserId();
            message = item.getDescription();
            postedDate = item.getCreatedAt() != null
                    ? item.getCreatedAt()
                    : item.getDateLost() != null ? item.getDateLost().atStartOfDay() : null;
        }

        User poster = userRepository.findById(posterId)
                .orElseThrow(() -> new ResourceNotFoundException("Poster not found"));

        return PosterDetailsResponse.builder()
                .posterId(poster.getId())
                .name(poster.getName())
                .email(poster.getEmail())
                .phone(poster.getPhone())
                .postedDate(postedDate)
                .message(message)
                .build();
    }

    public ClaimResponseDto createRequest(ClaimRequestDto request, Long authenticatedUserId) {
        String itemType = resolveItemType(request.getItemId(), request.getItemType());
        boolean isFoundItem = "found".equalsIgnoreCase(itemType);
        User claimant = userRepository.findById(request.getClaimerId())
                .orElseThrow(() -> new ResourceNotFoundException("Claimant not found"));
        Long posterId;
        Long itemId = request.getItemId();
        ItemStatus itemStatus;

        if (isFoundItem) {
            FoundItem item = foundItemService.getEntityById(itemId);
            posterId = item.getUserId();
            itemStatus = item.getStatus();
        } else {
            LostItem item = lostItemService.getEntityById(itemId);
            posterId = item.getUserId();
            itemStatus = item.getStatus();
        }

        if (!authenticatedUserId.equals(request.getClaimerId())) {
            throw new BadRequestException("Claim requester does not match logged-in user");
        }
        if (posterId.equals(request.getClaimerId())) {
            throw new BadRequestException("You cannot claim your own post");
        }
        if (itemStatus == ItemStatus.CLAIMED || itemStatus == ItemStatus.CLOSED) {
            throw new BadRequestException("This item is no longer available for claims");
        }
        if (claimRequestRepository.existsByItemIdAndClaimantIdAndStatus(itemId, request.getClaimerId(), ClaimStatus.PENDING)) {
            throw new BadRequestException("You already have a pending claim request for this item");
        }

        String claimantPhone = request.getClaimerPhone();
        if (claimantPhone == null || claimantPhone.isBlank()) {
            claimantPhone = claimant.getPhone();
        }
        if (claimantPhone == null || claimantPhone.isBlank()) {
            throw new BadRequestException("Please provide a mobile number so the reporter can contact you");
        }

        ClaimRequest saved = claimRequestRepository.save(ClaimRequest.builder()
                .itemId(itemId)
                .itemType(itemType)
                .posterId(posterId)
                .claimantId(request.getClaimerId())
                .claimantPhone(claimantPhone)
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .status(ClaimStatus.PENDING)
                .build());
        if (isFoundItem) {
            foundItemService.markMatched(itemId);
        } else {
            lostItemService.markMatched(itemId);
        }
        return map(saved);
    }

    public List<ClaimResponseDto> getAll() {
        return claimRequestRepository.findAll().stream()
                .sorted(Comparator.comparing(ClaimRequest::getCreatedAt).reversed())
                .map(this::map)
                .toList();
    }

    public List<ClaimResponseDto> getClaimsForPoster(Long posterId) {
        return claimRequestRepository.findByPosterIdOrderByCreatedAtDesc(posterId).stream()
                .map(this::map)
                .toList();
    }

    public List<ClaimResponseDto> getClaimsForClaimant(Long claimantId) {
        return claimRequestRepository.findByClaimantIdOrderByCreatedAtDesc(claimantId).stream()
                .map(this::map)
                .toList();
    }

    public ClaimResponseDto approve(Long id, Long posterId) {
        ClaimRequest claim = claimRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim request not found"));
        if (!claim.getPosterId().equals(posterId)) {
            throw new BadRequestException("Only the original reporter can approve this claim");
        }
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only pending claims can be approved");
        }

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setResolvedAt(LocalDateTime.now());
        claimRequestRepository.save(claim);

        List<ClaimRequest> pendingClaims = claimRequestRepository.findByItemIdAndStatus(claim.getItemId(), ClaimStatus.PENDING);
        for (ClaimRequest pendingClaim : pendingClaims) {
            if (!pendingClaim.getId().equals(claim.getId())) {
                pendingClaim.setStatus(ClaimStatus.REJECTED);
                pendingClaim.setResolvedAt(LocalDateTime.now());
                claimRequestRepository.save(pendingClaim);
            }
        }

        String itemType = resolveItemType(claim.getItemId(), claim.getItemType());
        if ("found".equalsIgnoreCase(itemType)) {
            foundItemService.handOverToClaimant(claim.getItemId(), claim.getClaimantId());
        } else {
            lostItemService.handOverToClaimant(claim.getItemId(), claim.getClaimantId());
        }
        return map(claim);
    }

    private ClaimResponseDto map(ClaimRequest claim) {
        String itemType = resolveItemType(claim.getItemId(), claim.getItemType());
        String itemTitle = "Item #" + claim.getItemId();
        if ("found".equalsIgnoreCase(itemType)) {
            itemTitle = foundItemService.getEntityById(claim.getItemId()).getTitle();
        } else {
            itemTitle = lostItemService.getEntityById(claim.getItemId()).getTitle();
        }
        User poster = userRepository.findById(claim.getPosterId()).orElse(null);
        User claimant = userRepository.findById(claim.getClaimantId()).orElse(null);

        return ClaimResponseDto.builder()
                .id(claim.getId())
                .itemId(claim.getItemId())
                .itemType(itemType)
                .itemTitle(itemTitle)
                .posterId(claim.getPosterId())
                .posterName(poster != null ? poster.getName() : null)
                .posterPhone(poster != null ? poster.getPhone() : null)
                .claimantId(claim.getClaimantId())
                .claimantName(claimant != null ? claimant.getName() : null)
                .claimantPhone(claim.getClaimantPhone() != null && !claim.getClaimantPhone().isBlank()
                        ? claim.getClaimantPhone()
                        : claimant != null ? claimant.getPhone() : null)
                .message(claim.getMessage())
                .status(claim.getStatus())
                .createdAt(claim.getCreatedAt())
                .resolvedAt(claim.getResolvedAt())
                .build();
    }

    private String resolveItemType(Long itemId, String requestedType) {
        if ("found".equalsIgnoreCase(requestedType)) {
            try {
                foundItemService.getEntityById(itemId);
                return "found";
            } catch (ResourceNotFoundException ignored) {
            }
        }

        if ("lost".equalsIgnoreCase(requestedType)) {
            try {
                lostItemService.getEntityById(itemId);
                return "lost";
            } catch (ResourceNotFoundException ignored) {
            }
        }

        try {
            foundItemService.getEntityById(itemId);
            return "found";
        } catch (ResourceNotFoundException ignored) {
        }

        try {
            lostItemService.getEntityById(itemId);
            return "lost";
        } catch (ResourceNotFoundException ignored) {
        }

        if ("found".equalsIgnoreCase(requestedType)) {
            throw new ResourceNotFoundException("Found item not found");
        }

        throw new ResourceNotFoundException("Lost item not found");
    }
}
