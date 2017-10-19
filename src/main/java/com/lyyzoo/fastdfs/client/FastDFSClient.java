package com.lyyzoo.fastdfs.client;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ProtoCommon;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FastDFS Java API. 文件上传下载主类.
 * <p>
 *
 * @author jiangzhou.bo@hand-china.com
 * @version 1.0
 * @name FastDFSClient
 * @date 2017-10-12 10:13
 */
public class FastDFSClient {
    /**
     * 路径分隔符
     */
    public static final String SEPARATOR = "/";
    /**
     * Point
     */
    public static final String POINT = ".";
    /**
     * ContentType
     */
    public static final Map<String, String> EXT_MAPS = new HashMap<>();

    /**
     * org.slf4j.Logger
     */
    private static Logger logger = LoggerFactory.getLogger(FastDFSClient.class);
    /**
     * 文件名称Key
     */
    private static final String FILENAME = "filename";
    /**
     * 文件最大的大小
     */
    private int maxFileSize = 100 * 1000 * 1000;

    public FastDFSClient() {
        initExt();
    }

    private void initExt() {
        // image
        EXT_MAPS.put("png", "image/png");
        EXT_MAPS.put("gif", "image/gif");
        EXT_MAPS.put("bmp", "image/bmp");
        EXT_MAPS.put("ico", "image/x-ico");
        EXT_MAPS.put("jpeg", "image/jpeg");
        EXT_MAPS.put("jpg", "image/jpeg");
        // 压缩文件
        EXT_MAPS.put("zip", "application/zip");
        EXT_MAPS.put("rar", "application/x-rar");
        // doc
        EXT_MAPS.put("pdf", "application/pdf");
        EXT_MAPS.put("ppt", "application/vnd.ms-powerpoint");
        EXT_MAPS.put("xls", "application/vnd.ms-excel");
        EXT_MAPS.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXT_MAPS.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        EXT_MAPS.put("doc", "application/msword");
        EXT_MAPS.put("doc", "application/wps-office.doc");
        EXT_MAPS.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXT_MAPS.put("txt", "text/plain");
        // 音频
        EXT_MAPS.put("mp4", "video/mp4");
        EXT_MAPS.put("flv", "video/x-flv");
    }

    /**
     * MultipartFile 上传文件
     *
     * @param file MultipartFile
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithMultipart(MultipartFile file) throws FastDFSException {
        return upload(file, null);
    }

    /**
     * MultipartFile 上传文件
     *
     * @param file MultipartFile
     * @param descriptions 文件描述
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithMultipart(MultipartFile file, Map<String, String> descriptions) throws FastDFSException {
        return upload(file, descriptions);
    }

    /**
     * 根据指定的路径上传文件
     *
     * @param filepath 文件路径
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithFilepath(String filepath) throws FastDFSException {
        return upload(filepath, null);
    }

    /**
     * 根据指定的路径上传文件
     *
     * @param filepath 文件路径
     * @param descriptions 文件描述
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithFilepath(String filepath, Map<String, String> descriptions) throws FastDFSException {
        return upload(filepath, descriptions);
    }

    /**
     * 上传base64文件
     *
     * @param base64 文件base64
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithBase64(String base64) throws FastDFSException {
        return upload(base64, null, null);
    }

    /**
     * 上传base64文件
     *
     * @param base64 文件base64
     * @param filename 文件名
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithBase64(String base64, String filename) throws FastDFSException {
        return upload(base64, filename, null);
    }

    /**
     * 上传base64文件
     *
     * @param base64 文件base64
     * @param filename 文件名
     * @param descriptions 文件描述信息
     * @return 返回上传成功后的文件路径
     */
    public String uploadFileWithBase64(String base64, String filename, Map<String, String> descriptions) throws FastDFSException {
        return upload(base64, filename, descriptions);
    }

    /**
     * 使用 MultipartFile 上传
     *
     * @param file MultipartFile
     * @param descriptions 文件描述信息
     * @return 文件路径
     * @throws FastDFSException file为空则抛出异常
     */
    public String upload(MultipartFile file, Map<String, String> descriptions) throws FastDFSException {
        if(file == null || file.isEmpty()){
            throw new FastDFSException(ErrorCode.FILE_ISNULL.CODE, ErrorCode.FILE_ISNULL.MESSAGE);
        }
        String path = null;
        try {
            path = upload(file.getInputStream(), file.getOriginalFilename(), descriptions);
        } catch (IOException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_ISNULL.CODE, ErrorCode.FILE_ISNULL.MESSAGE);
        }
        return path;
    }

    /**
     * 根据指定的路径上传
     *
     * @param filepath 文件路径
     * @param descriptions 文件描述
     * @return 文件路径
     * @throws FastDFSException 文件路径为空则抛出异常
     */
    public String upload(String filepath, Map<String, String> descriptions) throws FastDFSException {
        if(StringUtils.isBlank(filepath)){
            throw new FastDFSException(ErrorCode.FILE_PATH_ISNULL.CODE, ErrorCode.FILE_PATH_ISNULL.MESSAGE);
        }
        File file = new File(filepath);
        String path = null;
        try {
            InputStream is = new FileInputStream(file);
            // 获取文件名
            filepath = toLocal(filepath);
            String filename = filepath.substring(filepath.lastIndexOf("/") + 1);

            path = upload(is, filename, descriptions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_NOT_EXIST.CODE, ErrorCode.FILE_NOT_EXIST.MESSAGE);
        }

        return path;
    }

    /**
     *
     * 上传base64文件
     *
     * @param base64
     * @param filename 文件名
     * @param descriptions 文件描述信息
     * @return 文件路径
     * @throws FastDFSException base64为空则抛出异常
     */
    public String upload(String base64, String filename, Map<String, String> descriptions) throws FastDFSException {
        if(StringUtils.isBlank(base64)){
            throw new FastDFSException(ErrorCode.FILE_ISNULL.CODE, ErrorCode.FILE_ISNULL.MESSAGE);
        }
        return upload(new ByteArrayInputStream(Base64.decodeBase64(base64)), filename, descriptions);
    }

    /**
     * 上传通用方法
     *
     * @param is 文件输入流
     * @param filename 文件名
     * @param descriptions 文件描述信息
     * @return 组名+文件路径，如：group1/M00/00/00/wKgz6lnduTeAMdrcAAEoRmXZPp870.jpeg
     * @throws FastDFSException
     */
    public String upload(InputStream is, String filename, Map<String, String> descriptions) throws FastDFSException {
        if(is == null){
            throw new FastDFSException(ErrorCode.FILE_ISNULL.CODE, ErrorCode.FILE_ISNULL.MESSAGE);
        }

        try {
            if(is.available() > maxFileSize){
                throw new FastDFSException(ErrorCode.FILE_OUT_SIZE.CODE, ErrorCode.FILE_OUT_SIZE.MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        filename = toLocal(filename);
        // 返回路径
        String path = null;
        // 文件描述
        NameValuePair[] nvps = null;
        List<NameValuePair> nvpsList = new ArrayList<>();
        // 文件名后缀
        String suffix = getFilenameSuffix(filename);

        // 文件名
        if (StringUtils.isNotBlank(filename)) {
            nvpsList.add(new NameValuePair(FILENAME, filename));
        }
        // 描述信息
        if (descriptions != null && descriptions.size() > 0) {
            descriptions.forEach((key, value) -> {
                nvpsList.add(new NameValuePair(key, value));
            });
        }
        if (nvpsList.size() > 0) {
            nvps = new NameValuePair[nvpsList.size()];
            nvpsList.toArray(nvps);
        }

        TrackerServer trackerServer = TrackerServerPool.borrowObject();
        StorageClient1 storageClient = new StorageClient1(trackerServer, null);
        try {
            // 读取流
            byte[] fileBuff = new byte[is.available()];
            is.read(fileBuff, 0, fileBuff.length);

            // 上传
            path = storageClient.upload_file1(fileBuff, suffix, nvps);

            if(StringUtils.isBlank(path)) {
                throw new FastDFSException(ErrorCode.FILE_UPLOAD_FAILED.CODE, ErrorCode.FILE_UPLOAD_FAILED.MESSAGE);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("upload file success, return path is {}", path);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_UPLOAD_FAILED.CODE, ErrorCode.FILE_UPLOAD_FAILED.MESSAGE);
        } catch (MyException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_UPLOAD_FAILED.CODE, ErrorCode.FILE_UPLOAD_FAILED.MESSAGE);
        } finally {
            // 关闭流
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 返还对象
        TrackerServerPool.returnObject(trackerServer);

        return path;
    }

    /**
     * 以附件形式下载文件
     *
     * @param filepath 文件路径
     * @param response
     */
    public void downloadFile(String filepath, HttpServletResponse response) throws FastDFSException {
        download(filepath, null, null, response);
    }

    /**
     * 下载文件 输出文件
     *
     * @param filepath 文件路径
     * @param os 输出流
     */
    public void downloadFile(String filepath, OutputStream os) throws FastDFSException {
        download(filepath, null, os, null);
    }

    /**
     * 以附件形式下载文件 可以指定文件名称.
     *
     * @param filepath 文件路径
     * @param filename 文件名称
     * @param response HttpServletResponse
     */
    public void downloadFile(String filepath, String filename, HttpServletResponse response) throws FastDFSException {
        download(filepath, filename, null, response);
    }

    /**
     * 下载文件
     *
     * @param filepath 文件路径
     * @param filename 文件名称
     * @param os 输出流
     * @param response HttpServletResponse
     */
    public void download(String filepath, String filename, OutputStream os, HttpServletResponse response) throws FastDFSException {
        if(StringUtils.isBlank(filepath)){
            throw new FastDFSException(ErrorCode.FILE_PATH_ISNULL.CODE, ErrorCode.FILE_PATH_ISNULL.MESSAGE);
        }

        filepath = toLocal(filepath);
        // 文件名
        if (StringUtils.isBlank(filename)) {
            filename = getOriginalFilename(filepath);
        }
        String contentType = EXT_MAPS.get(getFilenameSuffix(filename));

        if(logger.isDebugEnabled()){
            logger.debug("download file, filepath = {}, filename = {}", filepath, filename);
        }

        TrackerServer trackerServer = TrackerServerPool.borrowObject();
        StorageClient1 storageClient = new StorageClient1(trackerServer, null);
        InputStream is = null;
        try {
            // 下载
            byte[] fileByte = storageClient.download_file1(filepath);

            if(fileByte == null){
                throw new FastDFSException(ErrorCode.FILE_NOT_EXIST.CODE, ErrorCode.FILE_NOT_EXIST.MESSAGE);
            }

            if (response != null) {
                os = response.getOutputStream();

                // 设置响应头
                if (StringUtils.isNotBlank(contentType)) {
                    // 文件编码 处理文件名中的 '+'、' ' 特殊字符
                    String encoderName = URLEncoder.encode(filename, "UTF-8").replace("+", "%20").replace("%2B", "+");
                    response.setHeader("Content-Disposition", "attachment;filename=\"" + encoderName + "\"");
                    response.setContentType(contentType + ";charset=UTF-8");
                    response.setHeader("Accept-Ranges", "bytes");
                }
            }

            is = new ByteArrayInputStream(fileByte);
            byte[] buffer = new byte[1024 * 5];
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_DOWNLOAD_FAILED.CODE, ErrorCode.FILE_DOWNLOAD_FAILED.MESSAGE);
        } finally {
            // 关闭流
            try {
                if(is != null){
                    is.close();
                }
                if(os != null){
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 返还对象
        TrackerServerPool.returnObject(trackerServer);
    }

    /**
     * 下载文件
     *
     * @param filepath 文件路径
     * @return 返回文件字节
     * @throws FastDFSException
     */
    public byte[] download(String filepath) throws FastDFSException {
        if(StringUtils.isBlank(filepath)){
            throw new FastDFSException(ErrorCode.FILE_PATH_ISNULL.CODE, ErrorCode.FILE_PATH_ISNULL.MESSAGE);
        }

        TrackerServer trackerServer = TrackerServerPool.borrowObject();
        StorageClient1 storageClient = new StorageClient1(trackerServer, null);
        InputStream is = null;
        byte[] fileByte = null;
        try {
            fileByte = storageClient.download_file1(filepath);

            if(fileByte == null){
                throw new FastDFSException(ErrorCode.FILE_NOT_EXIST.CODE, ErrorCode.FILE_NOT_EXIST.MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_DOWNLOAD_FAILED.CODE, ErrorCode.FILE_DOWNLOAD_FAILED.MESSAGE);
        }
        // 返还对象
        TrackerServerPool.returnObject(trackerServer);

        return fileByte;
    }

    /**
     * 删除文件
     *
     * @param filepath 文件路径
     * @return 删除成功返回 0, 失败返回其它
     */
    public int deleteFile(String filepath) throws FastDFSException {
        if(StringUtils.isBlank(filepath)){
            throw new FastDFSException(ErrorCode.FILE_PATH_ISNULL.CODE, ErrorCode.FILE_PATH_ISNULL.MESSAGE);
        }

        TrackerServer trackerServer = TrackerServerPool.borrowObject();
        StorageClient1 storageClient = new StorageClient1(trackerServer, null);
        int success = 0;
        try {
            success = storageClient.delete_file1(filepath);
            if(success != 0){
                throw new FastDFSException(ErrorCode.FILE_DELETE_FAILED.CODE, ErrorCode.FILE_DELETE_FAILED.MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
            throw new FastDFSException(ErrorCode.FILE_DELETE_FAILED.CODE, ErrorCode.FILE_DELETE_FAILED.MESSAGE);
        }
        // 返还对象
        TrackerServerPool.returnObject(trackerServer);

        return success;
    }

    /**
     * 获取文件信息
     * 
     * @param filepath 文件路径
     * @return 文件信息
     * 
     * <pre>
     *  {<br>
     *      "SourceIpAddr": 源IP <br>
     *      "FileSize": 文件大小 <br>
     *      "CreateTime": 创建时间 <br>
     *      "CRC32": 签名 <br>
     *  }  <br>
     * </pre>
     */
    public Map<String, Object> getFileInfo(String filepath) throws FastDFSException {
        TrackerServer trackerServer = TrackerServerPool.borrowObject();
        StorageClient1 storageClient = new StorageClient1(trackerServer, null);
        FileInfo fileInfo = null;
        try {
            fileInfo = storageClient.get_file_info1(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        // 返还对象
        TrackerServerPool.returnObject(trackerServer);

        Map<String, Object> infoMap = new HashMap<>(4);

        infoMap.put("SourceIpAddr", fileInfo.getSourceIpAddr());
        infoMap.put("FileSize", fileInfo.getFileSize());
        infoMap.put("CreateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fileInfo.getCreateTimestamp()));
        infoMap.put("CRC32", fileInfo.getCrc32());

        return infoMap;
    }

    /**
     * 获取文件描述信息
     * 
     * @param filepath 文件路径
     * @return 文件描述信息
     */
    public Map<String, Object> getFileDescriptions(String filepath) throws FastDFSException {
        TrackerServer trackerServer = TrackerServerPool.borrowObject();
        StorageClient1 storageClient = new StorageClient1(trackerServer, null);
        NameValuePair[] nvps = null;
        try {
            nvps = storageClient.get_metadata1(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        // 返还对象
        TrackerServerPool.returnObject(trackerServer);

        Map<String, Object> infoMap = null;

        if (nvps != null && nvps.length > 0) {
            infoMap = new HashMap<>(nvps.length);

            for (NameValuePair nvp : nvps) {
                infoMap.put(nvp.getName(), nvp.getValue());
            }
        }

        return infoMap;
    }

    /**
     * 获取源文件的文件名称
     * 
     * @param filepath 文件路径
     * @return 文件名称
     */
    public String getOriginalFilename(String filepath) throws FastDFSException {
        Map<String, Object> descriptions = getFileDescriptions(filepath);
        if (descriptions.get(FILENAME) != null) {
            return (String) descriptions.get(FILENAME);
        }
        return null;
    }

    /**
     * 获取文件名称的后缀
     *
     * @param filename 文件名 或 文件路径
     * @return 文件后缀
     */
    public static String getFilenameSuffix(String filename) {
        String suffix = null;
        String originalFilename = filename;
        if (StringUtils.isNotBlank(filename)) {
            if (filename.contains(SEPARATOR)) {
                filename = filename.substring(filename.lastIndexOf(SEPARATOR) + 1);
            }
            if (filename.contains(POINT)) {
                suffix = filename.substring(filename.lastIndexOf(POINT) + 1);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("filename error without suffix : {}", originalFilename);
                }
            }
        }
        return suffix;
    }

    /**
     * 转换路径中的 '\' 为 '/' <br>
     * 并把文件后缀转为小写
     *
     * @param path 路径
     * @return
     */
    public static String toLocal(String path) {
        if (StringUtils.isNotBlank(path)) {
            path = path.replaceAll("\\\\", SEPARATOR);

            if (path.contains(POINT)) {
                String pre = path.substring(0, path.lastIndexOf(POINT) + 1);
                String suffix = path.substring(path.lastIndexOf(POINT) + 1).toLowerCase();
                path = pre + suffix;
            }
        }
        return path;
    }

    /**
     * 获取FastDFS文件的名称，如：M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     *
     * @param fileId 包含组名和文件名，如：group1/M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     * @return FastDFS 返回的文件名：M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     */
    public static String getFilename(String fileId){
        String[] results = new String[2];
        StorageClient1.split_file_id(fileId, results);

        return results[1];
    }

    /**
     * 获取访问服务器的token，拼接到地址后面
     *
     * @param filepath 文件路径 group1/M00/00/00/wKgzgFnkTPyAIAUGAAEoRmXZPp876.jpeg
     * @param httpSecretKey 秘钥
     * @return 返回token，如： token=078d370098b03e9020b82c829c205e1f&ts=1508141521
     */
    public static String getToken(String filepath, String httpSecretKey){
        // unix seconds
        int ts = (int) Instant.now().getEpochSecond();
        // token
        String token = "null";
        try {
            token = ProtoCommon.getToken(getFilename(filepath), ts, httpSecretKey);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("token=").append(token);
        sb.append("&ts=").append(ts);

        return sb.toString();
    }

    /**
     * @return the max file size
     */
    public int getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Set max file size, default 100M
     * @param maxFileSize the max file size
     */
    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * 测试
     */
    public static void main(String[] args) throws Exception {


    }

}
