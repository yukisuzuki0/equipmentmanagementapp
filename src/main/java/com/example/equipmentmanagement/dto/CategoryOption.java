package com.example.equipmentmanagement.dto;

public class CategoryOption {
    private String code;
    private String label;

    public CategoryOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
