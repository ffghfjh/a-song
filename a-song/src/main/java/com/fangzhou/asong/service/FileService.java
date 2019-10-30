package com.fangzhou.asong.service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;

public interface FileService {
    /**
     * 上传图片
     * @param file
     * @param extension 后缀
     * @return
     */

    String saveFile(MultipartFile file,String extension);

    int getFileType(MultipartFile file);

    String uploadFile(byte[] bytes, long fileSize, String extension);

    byte[] downloadFile(String fileUrl);

}
