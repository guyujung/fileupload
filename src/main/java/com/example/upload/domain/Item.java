package com.example.upload.domain;

import lombok.Data;
import lombok.Setter;

import java.util.List;

@Setter
@Data
public class Item {

    private Long id;
    private String itemName;
    private UploadFile attachFile;
    private List<UploadFile> imageFiles;
}
