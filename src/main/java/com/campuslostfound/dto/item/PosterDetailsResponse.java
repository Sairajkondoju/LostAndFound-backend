package com.campuslostfound.dto.item;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PosterDetailsResponse {
    private Long posterId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime postedDate;
    private String message;
}
