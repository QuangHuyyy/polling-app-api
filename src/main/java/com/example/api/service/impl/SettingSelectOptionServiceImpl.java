package com.example.api.service.impl;

import com.example.api.exception.ResourceNotFoundException;
import com.example.api.model.SettingSelectOption;
import com.example.api.repository.ISettingSelectOptionRepository;
import com.example.api.service.ISettingSelectOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingSelectOptionServiceImpl implements ISettingSelectOptionService {
    private final ISettingSelectOptionRepository settingSelectOptionRepository;
    @Override
    public SettingSelectOption getByValue(String value) {
        return settingSelectOptionRepository.findByValue(value).orElseThrow(() -> new ResourceNotFoundException("Setting select option", "value", value));
    }
}
