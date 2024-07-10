package az.CartEr.Service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UploadImageService {
    private static final String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/todoimages-d87af.appspot.com/o/"; // Placeholder for download URL
    private String TEMP_URL; // Placeholder for temporary URL storage

    private String uploadFile(InputStream inputStream, String fileName) throws IOException {
        BlobId blobId = BlobId.of("todoimages-d87af.appspot.com", fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("google-services (1).json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        storage.create(blobInfo, inputStream);
        return String.format((DOWNLOAD_URL + fileName + "?alt=media"), URLEncoder.encode(fileName, StandardCharsets.UTF_8));
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public Object upload(MultipartFile multipartFile) {
        try {
            String fileName = multipartFile.getOriginalFilename();
            fileName = UUID.randomUUID().toString().concat(this.getExtension(fileName));

            try (InputStream inputStream = multipartFile.getInputStream()) {
                TEMP_URL = this.uploadFile(inputStream, fileName);
            }
            return TEMP_URL;
        } catch (Exception e) {
            e.printStackTrace();
            return "Unsuccessfully Uploaded! Error: " + e.getMessage();
        }
    }

}
