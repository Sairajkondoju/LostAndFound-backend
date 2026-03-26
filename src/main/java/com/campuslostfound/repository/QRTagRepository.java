package com.campuslostfound.repository;

import com.campuslostfound.entity.QRTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QRTagRepository extends JpaRepository<QRTag, Long> {
    Optional<QRTag> findByTagId(String tagId);
    List<QRTag> findByUserId(Long userId);
}
