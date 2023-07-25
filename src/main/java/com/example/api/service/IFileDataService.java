package com.example.api.service;

import com.example.api.model.FileData;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IFileDataService {
    void init();
    boolean checkFilenameExist(String filename);
    FileData save(MultipartFile file);
    FileData copy(String filename);
    Resource load(String filename);
    byte[] display(String filename);
    Resource download(String filename);
    boolean delete(String filename);

    String getUrlFile(String filename);


}
