package com.campuslostfound.repository;

import com.campuslostfound.entity.ClaimRequest;
import com.campuslostfound.entity.ClaimStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRequestRepository extends JpaRepository<ClaimRequest, Long> {
    boolean existsByItemIdAndClaimantIdAndStatus(Long itemId, Long claimantId, ClaimStatus status);
    List<ClaimRequest> findByPosterIdOrderByCreatedAtDesc(Long posterId);
    List<ClaimRequest> findByClaimantIdOrderByCreatedAtDesc(Long claimantId);
    List<ClaimRequest> findByItemIdAndStatus(Long itemId, ClaimStatus status);
}
