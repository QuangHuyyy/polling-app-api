package com.example.api.service;

import com.example.api.model.SettingSelectOption;

public interface ISettingSelectOptionService {
    SettingSelectOption getByValue(String value);
}
