package com.campuslostfound.dto.claim;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ClaimRequestDto {
    @NotNull
    private Long itemId;

    @NotBlank
    private String itemType;

    @NotNull
    private Long claimerId;

    private String claimerPhone;

    @NotBlank
    private String message;
}
