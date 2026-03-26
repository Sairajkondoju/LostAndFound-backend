package com.campuslostfound.dto.claim;

import com.campuslostfound.entity.ClaimStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimResponseDto {
    private Long id;
    private Long itemId;
    private String itemType;
    private String itemTitle;
    private Long posterId;
    private String posterName;
    private String posterPhone;
    private Long claimantId;
    private String claimantName;
    private String claimantPhone;
    private String message;
    private ClaimStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
