package com.fiap.hackaton.grp14.consumer.repository;

import com.fiap.hackaton.grp14.consumer.model.Video;
import com.fiap.hackaton.grp14.consumer.model.VideoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByStatus(VideoStatus status);

    Optional<Video> findByFilename(String filename);
}

