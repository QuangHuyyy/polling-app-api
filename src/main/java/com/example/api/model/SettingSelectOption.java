package com.example.api.model;

import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "setting_select_option")
public class SettingSelectOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ESettingSelectType settingSelectType;
}
