package com.example.api.service;

import java.io.ByteArrayInputStream;

public interface IExcelService {
    ByteArrayInputStream load(String pollUuid, String userUuid);
}
