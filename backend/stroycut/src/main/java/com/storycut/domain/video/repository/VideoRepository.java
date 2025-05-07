package com.storycut.domain.video.repository;

import com.storycut.domain.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    
    List<Video> findByMemberId(Long memberId);
    
    List<Video> findByOriginalVideoId(Long originalVideoId);
    
}
