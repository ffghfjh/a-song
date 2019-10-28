package com.fangzhou.filesystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface FastFileService {

    /**
     * 储存图片
     * @param img 图片文件
     * @return
     */
    String saveImages(MultipartFile img);

    /**
     * 下载文件
     * @param fileUrl 文件在fastDFS的ID
     * @return
     */
    byte[] getFile(String fileUrl);

}
