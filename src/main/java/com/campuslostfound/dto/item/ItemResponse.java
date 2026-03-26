package com.campuslostfound.dto.item;

import com.campuslostfound.entity.ItemStatus;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponse {
    private Long id;
    private String type;
    private String title;
    private String description;
    private String location;
    private LocalDate date;
    private String imageUrl;
    private ItemStatus status;
    private Long userId;
}
