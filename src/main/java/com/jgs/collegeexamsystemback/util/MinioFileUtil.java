package com.jgs.collegeexamsystemback.util;

import cn.hutool.core.util.RandomUtil;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * minio文件存储工具类
 */
public class MinioFileUtil {
    private static final String ENDPOINT = "http://192.168.239.128:9000";
    private static final String ACCESS_KEY = "9PY2lIRHn9puGke9TAmK";
    private static final String SECRET_KEY = "MPthTUdPxDskxqbxTP8raM6gsDmHrX4YfEWFOhFs";
    private static final String BUCKET = "college-exam-system-user-images";
    private static final String FILE_URL = "http://192.168.239.128:9000/college-exam-system-user-images/";
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    // 新建minio client
    private MinioClient createMinioClient() throws InvalidKeyException,IOException,NoSuchAlgorithmException{
        try {
            // Create a minioClient with the MinIO server playground, its access key and secret key
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(ENDPOINT)
                    .credentials(ACCESS_KEY,SECRET_KEY)
                    .build();
            // Make 'college-exam-system-user-images' bucket if not exist
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("college-exam-system-user-images").build());
            if (!found){
                // Make a new bucket called 'college-exam-system-user-images'
                minioClient.makeBucket(MakeBucketArgs.builder().bucket("college-exam-system-user-images").build());
            }else {
                System.out.println("Bucket 'college-exam-system-user-images' already exists.");
            }
            return minioClient;
        } catch (MinioException e) {
            throw new RuntimeException(e);
        }
    }
    // minio 默认图片上传
    public String uploadDefaultImage() throws InvalidKeyException, IOException, NoSuchAlgorithmException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient client = createMinioClient();
        // Upload file
        File file = new File("src/main/resources/static/touxiang.png");
        String fileName = format.format(new Date()) + "_" + RandomUtil.randomString(10) + "_" + file.getName();
        client.uploadObject(UploadObjectArgs.builder()
                .bucket(BUCKET)
                .object(fileName)
                .filename(file.getAbsolutePath())
                .contentType("image/jpeg")
            .build());
        return FILE_URL + fileName;
    }
    // minio 图片上传
    public String uploadImage(MultipartFile file) throws InvalidKeyException, IOException, NoSuchAlgorithmException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient client = createMinioClient();
        // Upload file
        String fileName = format.format(new Date()) + "_" + RandomUtil.randomString(10) + "_" + file.getOriginalFilename();
        client.putObject(PutObjectArgs.builder()
                .bucket(BUCKET)
                .stream(file.getInputStream(), file.getSize(), 0)
                .object(fileName)
                .contentType(file.getContentType()).build());
        return FILE_URL + fileName;
    }
    // minio 图片删除
    public boolean removeImage(String fileUrl) throws IOException, NoSuchAlgorithmException, InvalidKeyException, ServerException, InsufficientDataException, ErrorResponseException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient client = createMinioClient();
        if (fileUrl.isEmpty()){
            return false;
        }
        // 解析fileUrl
        String[] split = fileUrl.split("/");
        String fileName = split[4];
        client.removeObject(RemoveObjectArgs.builder()
                .bucket(BUCKET)
                .object(fileName)
                .build());
        return true;
    }

    // minio 多图片删除
    public boolean removeImages(List<String> imageUrls) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        MinioClient client = createMinioClient();
        if (imageUrls.isEmpty()){
            return false;
        }
        imageUrls.forEach(imageUrl -> {
            String[] split = imageUrl.split("/");
            String fileName = split[4];
            try {
                client.removeObject(RemoveObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(fileName)
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return true;
    }
}
