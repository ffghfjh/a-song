package com.fangzhou.filesystem.service.impl;

import com.fangzhou.filesystem.component.FastDFSClientWrapper;
import com.fangzhou.filesystem.service.FastFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@Service
public class FastFileServiceImpl implements FastFileService {

    @Autowired
    FastDFSClientWrapper clientWrapper;

    Logger logger = LoggerFactory.getLogger(FastFileService.class);

    @Override
    public String saveImages(MultipartFile img) {
        String fileTyle = img.getContentType();
        logger.info("文件类型:" + img.getContentType());
        if (fileTyle.equals("image/jpeg") || fileTyle.equals("image/png")) {
            try {
                byte[] bytes = img.getBytes();
                String originalFileName = img.getOriginalFilename();
                String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
                return clientWrapper.uploadFile(img.getBytes(),img.getSize(),extension);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return "图片格式不正确";
        }
    }

    @Override
    public byte[] getFile(String fileUrl) {
        byte[] bytes = new byte[0];
        try {
            bytes = clientWrapper.downloadFile(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }
}
