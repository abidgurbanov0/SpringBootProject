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
    String googleServicesJson = "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"todoimages-d87af\",\n" +
            "  \"private_key_id\": \"856d734e98f6f3647d669678f110aa204f5fe706\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+wrV8lxD90Zux\\nNCjs3WDAsTYlyIIEwt8+MGxMUC029duJGad9E1rFtC3zh5OfYhEqdJxh+jX0IvnD\\nMxdBX5Nv6q1akAxAU6iJrZ7pugmFj81FWAZzK4Xqg+4u0J0tyDP3GtLOHI688/sn\\nWa5BkPJR3DFhpiv48vSu1bfZaLDMlmWtvyIWBUitKQFK03ITrCBJLh03NxIGQzVo\\nOQ7b4VMw1ddw874JEMWzpCq1C5PVCaGG6QkwS3nUgBgaaDr4cVwcNdamqSnRWgnN\\nXmO8SKAs1w5AeuSYkB1GBJeIJITRiLY/3ftFGWTyc0F2MzyKO5XPtJnoNTLX7DOd\\n2KG3GBsdAgMBAAECggEAE2LN4c2ntuZfKFOGLd1+OybnO1C/Udo3U2ZwDb3/fPyK\\n+nRbzpIHIVy4Z21+8ELIPs8fTgpeWE4DoYrm0Uh0FHq+HPF9pT3l51P4eIU6TtNN\\n3Om0rMNJYTYAyyDSkGKuUULtwG+A3AKpd+wdEYxi698bj50SY+2A4JGslOYW7ZDV\\nVjmzArOBHkXa16oUsg11EiMgs21jbZ8Rn4TEu4wGZtEVHR6Gq6jR6Zb/6aCU5F6G\\nP9ZJBwxLMwWTyJaWveV3FYHl3D/hkVelwItnC4ZzhQjGEYpDALHtCU5512NO94E/\\nygSf19q9ut7+3f9k4sbddk6t0vmCgJqM/tMUPtONywKBgQD3iunN1jQlzK/OFjyL\\nrWPkqDQ82WbHHELBTJHfktYsU/7w9rRhPAuVuJp5YZgKq/AU0a2NTQEGve83/MZx\\nv/NkNppmnTBZagFOcT9XQumssFBqQSsIU8lCHVYEE28SkVS29IKESCOzfm5uf1y0\\nYq/HBInQGIjkTs/7Ic6wMBTu6wKBgQDFRylojfQOpeiYA2ypn/BcMCkuEnW+8zUp\\nQQtJE9r8N0iQBh25mDhotHn37KGb19IQbp1vKkDWS8P8TaON5Jtszjf6trg1/eQ7\\nDFT6vpCd9Qqx7NyxleOV6uzdWcv56qia8Wxeq7tQ85wa1g255HToudOP7mkENl14\\nifnZKWLsFwKBgGeO1LsI5q39kHq98LxaYFTzTU87f/TCTOcC+m+2xSSsSxZRjyqy\\nbQJIxU8MVgf44CpFgb6APXKtmNsqQ3ao39DHyxYsomJ1p3BCjkYaNxnIdQvOkYsd\\nSi//h9GSs7/nzrHvXn6qk0zvEpHldEn+kxzGOEU3TmV81yNvhEFPDCLbAoGARFGo\\nnXTLmGcMbdiBwRyKeOKU4Ee0TzSarHbCGcSuibn7fyUfFi8JGfmmY52nOfk73kTw\\ncJcJbS0/6hhZE8KfsMaZzFGK6efLKkfTBiMIoitCUOveYgGulzSMYvu+EAab7Uy+\\nYurGK/qxjPmkN0RO2GnCQo4mADdzl1LxG6uhfnECgYEAjTWgXNbEpmRltFNzsAu+\\nmur49LEHuJtqwI3MVcen4B3ahjrlYxDNg06EDoiNbDJwk0kE5CPJNXf8e9THI4fm\\nl0G7t8zHJ9fwdf8wbQrNRu1bmr0ZtqynDZoRi2yAXkPWFW3lM6rLxPXTsZfqrU1z\\nGCvCHVHmnKKs+/gqJQ1cwfc=\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"firebase-adminsdk-y90do@todoimages-d87af.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"114589586777380345640\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-y90do%40todoimages-d87af.iam.gserviceaccount.com\",\n" +
            "  \"universe_domain\": \"googleapis.com\"\n" +
            "}";
    private String uploadFile(InputStream inputStream, String fileName) throws IOException {
        BlobId blobId = BlobId.of("todoimages-d87af.appspot.com", fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();
        InputStream jsonStream = new ByteArrayInputStream(googleServicesJson.getBytes(StandardCharsets.UTF_8));

        // Load credentials from the InputStream
        Credentials credentials = GoogleCredentials.fromStream(jsonStream);

        // Initialize the Storage instance
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
//
