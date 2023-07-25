package com.example.api.controller;

import com.example.api.exception.BadRequestException;
import com.example.api.exception.PermissionException;
import com.example.api.exception.ResourceNotFoundException;
import com.example.api.service.IExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@CrossOrigin(origins = {"https://quanghuy-polling-app.web.app", "http://localhost:8080"}, maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class ExcelController {
    private final IExcelService excelService;

    @GetMapping("/{uuid}/export")
    public ResponseEntity<Resource> exportMultipleImageAnswerResult(@PathVariable(name = "uuid") String pollUuid, @RequestParam() String userUuid) {
        try {
            String filename = "straw_poll-" + pollUuid + "-" + new Date().getTime() + ".xlsx";

            InputStreamResource file = new InputStreamResource(excelService.load(pollUuid, userUuid));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
        } catch (ResourceNotFoundException e){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (BadRequestException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (PermissionException e){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }
}
