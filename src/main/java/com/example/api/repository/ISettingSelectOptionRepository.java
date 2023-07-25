package com.example.api.repository;

import com.example.api.model.SettingSelectOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISettingSelectOptionRepository extends JpaRepository<SettingSelectOption, Long> {
    Optional<SettingSelectOption> findByValue(String value);
}
