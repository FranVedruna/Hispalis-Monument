package com.franline.hispalismonument.services;

import org.springframework.web.multipart.MultipartFile;

public interface IImageStorageService {
    public String store(MultipartFile file);

}
