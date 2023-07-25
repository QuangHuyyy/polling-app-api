package com.example.api.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class VotingTypeResponse {
    private String icon;
    private String label;
    private String value;

    public VotingTypeResponse(String icon, String label, String value) {
        this.icon = icon;
        this.label = label;
        this.value = value;
    }
}
