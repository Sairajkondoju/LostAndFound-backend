package com.campuslostfound.repository;

import com.campuslostfound.entity.LostItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    List<LostItem> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
