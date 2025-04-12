package com.fiap.hackaton.grp14.producer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fiap.hackaton.grp14.producer.model.Video;
import com.fiap.hackaton.grp14.producer.model.VideoStatus;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByStatus(VideoStatus status);

    Optional<Video> findByFilename(String filename);
}

