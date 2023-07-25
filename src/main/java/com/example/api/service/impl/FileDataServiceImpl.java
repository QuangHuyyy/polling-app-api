package com.example.api.service.impl;

import com.example.api.controller.FileController;
import com.example.api.exception.BadRequestException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.FileData;
import com.example.api.repository.IFileDataRepository;
import com.example.api.service.IFileDataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class FileDataServiceImpl implements IFileDataService {
    private final IFileDataRepository fileDataRepository;

    @Value("${application.upload.path}")
    private String getPath;
    private Path root;

    @PostConstruct
    @Override
    public void init() {
        try {
            root = Paths.get(getPath);

            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't initialize folder for upload!");
        }
    }

    @Override
    public boolean checkFilenameExist(String filename) {
        return fileDataRepository.existsByFilename(filename);
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }

        String[] filenameParts = filename.split("\\.");
        return filenameParts[filenameParts.length - 1];
    }

    @Override
    public FileData save(MultipartFile file) {
        try {
            String filename = new Date().getTime() + "-file." + getFileExtension(file.getOriginalFilename());

            if (filename.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + filename);
            }

//            root.resolve: nối têm file vào đường dẫn root
            Path targetLocation;

            targetLocation = root.resolve(filename);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileDataRepository.save(FileData.builder()
                    .filename(filename)
                    .url(targetLocation.toString())
                    .type(file.getContentType())
                    .size(Files.size(targetLocation))
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FileData copy(String filenameResource) {
        try {
            String filename = new Date().getTime() + "-file." + filenameResource.split("\\.")[1];

            if (filename.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + filename);
            }
            Path targetLocationCopy = root.resolve(filename);
            Path targetLocationResource = root.resolve(filenameResource);

            Files.copy(targetLocationResource, targetLocationCopy, StandardCopyOption.REPLACE_EXISTING);

            return fileDataRepository.save(FileData.builder()
                    .filename(filename)
                    .url(targetLocationCopy.toString())
                    .type(Files.probeContentType(targetLocationResource))
                    .size(Files.size(targetLocationCopy))
                    .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Error: " + e.getMessage());
        }
    }

    @Override
    public byte[] display(String filename) {
        try {
            FileData fileData = fileDataRepository.findByFilename(filename)
                    .orElseThrow(() -> new ResourceNotFoundException("File", "avatar", filename));
            Path file = Paths.get(fileData.getUrl());
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource download(String filename) {
        FileData fileData = fileDataRepository.findByFilename(filename)
                .orElseThrow(() -> new ResourceNotFoundException("File", "avatar", filename));
        Path file = Paths.get(fileData.getUrl());
        try {
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String filename) {
        try {
            Path path = root.resolve(filename);
            FileData fileData = fileDataRepository.findByFilename(filename).orElseThrow(() -> new ResourceNotFoundException("FileData", "filename", filename));
            fileDataRepository.delete(fileData);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Error: " + e);
        }
    }

    @Override
    public String getUrlFile(String filename) {
        return MvcUriComponentsBuilder
                .fromMethodName(FileController.class, "loadFile", filename)
                .build().toString();
    }
}
