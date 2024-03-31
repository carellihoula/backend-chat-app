package com.carel.backendapp.aws;


import com.carel.backendapp.user.User;
import com.carel.backendapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class StorageService {

    private final UserRepository userRepository;
    private static final List<String> extensionsAllow = List.of("jpg","jpeg", "png");
            
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private S3Client createS3Client(){
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                accessKey,
                secretKey
        );
        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of("eu-west-3"))
                .build();

    }

    public String uploadFileToAws(Integer id, MultipartFile multipartFile) throws IOException {
        //CHECK if it's a photo (jpg, jpeg, png)
        if(!isSupportedImageFormat(multipartFile)){
            throw new RuntimeException("format du format non autorisé");
        };
        //process to send file to AWS S3
        File fileObj = convertMultipartFileToFile(multipartFile);
        String filename = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        S3Client s3Client = createS3Client();

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(filename).build(), RequestBody.fromFile(fileObj));
        String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + filename;

        //save fileUrl in User Model
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User doesn't exist"));
        user.setProfileImage(fileUrl);
        userRepository.save(user);

        //Nettoyage du fichier temporaire
        Files.delete(fileObj.toPath());

        return fileUrl;
    }

    private static boolean isSupportedImageFormat(MultipartFile multipartFile) {
        String extensionFile = multipartFile.getOriginalFilename().substring(
                multipartFile.getOriginalFilename().lastIndexOf(".") + 1
        );
        return extensionFile.equals("jpg") || extensionFile.equals("jpeg") || extensionFile.equals("png");
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File convertedfile = new File(System.getProperty("java.io.tmpdir") + multipartFile.getOriginalFilename());

        try(
                FileOutputStream fos = new FileOutputStream(convertedfile);
                InputStream fis = multipartFile.getInputStream();
                ){
            byte[] buffer = new byte[1024];
            int readBytes;
            while((readBytes = fis.read(buffer)) != -1){
                fos.write(buffer, 0 , readBytes);
            }
        }

        convertedfile.deleteOnExit();
        return  convertedfile;
    }


}
