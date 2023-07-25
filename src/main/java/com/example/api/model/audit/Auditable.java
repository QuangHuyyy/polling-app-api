package com.example.api.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@Getter
@Setter
@MappedSuperclass
@JsonIgnoreProperties(
        value = {"createBy", "updateBy"},
        allowGetters = true
)
public class Auditable extends DateAudit{
    @CreatedBy
//    @Column(updatable = false) // không thể cập nhật
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
