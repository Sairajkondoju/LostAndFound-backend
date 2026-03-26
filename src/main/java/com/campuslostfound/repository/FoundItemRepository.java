package com.campuslostfound.repository;

import com.campuslostfound.entity.FoundItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    List<FoundItem> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
