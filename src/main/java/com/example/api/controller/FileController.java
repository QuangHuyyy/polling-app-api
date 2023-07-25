package com.example.api.controller;

import com.example.api.exception.BadRequestException;
import com.example.api.model.FileData;
import com.example.api.payload.response.ResponseMessage;
import com.example.api.service.IFileDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://quanghuy-polling-app.web.app", "http://localhost:8080"}, maxAge = 3600, allowCredentials="true")
public class FileController {
    private final IFileDataService fileDataService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file){
        String message;
        try {
            FileData fileData = fileDataService.save(file);

            return ResponseEntity.status(HttpStatus.OK).body(fileData);
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();

            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> loadFile(@PathVariable String filename) {
        try {
            Resource file = fileDataService.load(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @GetMapping("display/{filename:.+}")
    public ResponseEntity<byte[]> display(@PathVariable("filename") String fileName) {
        byte[] imageData = fileDataService.display(fileName);
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);
    }

    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<ResponseMessage> deleteFile(@PathVariable("filename") String filename){
        String message;

        try {
            boolean existed = fileDataService.delete(filename);

            if (existed) {
                message = "Delete the file successfully: " + filename;
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
            }

            message = "The file does not exist!";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not delete the file: " + filename + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message));
        }

    }
}
