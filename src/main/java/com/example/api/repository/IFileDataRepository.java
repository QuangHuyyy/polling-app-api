package com.example.api.repository;

import com.example.api.model.FileData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFileDataRepository extends JpaRepository<FileData, String> {
    Optional<FileData> findByFilename(String filename);

    Optional<FileData> findByUrl(String url);

    boolean existsByFilename(String filename);
}
