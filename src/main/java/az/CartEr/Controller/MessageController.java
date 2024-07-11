package az.CartEr.Controller;

import az.CartEr.Auth.TokenManager;
import az.CartEr.DTO.GetAllProductDto;
import az.CartEr.DTO.ProductDTO;
import az.CartEr.Model.ProductModel;
import az.CartEr.Model.User;
import az.CartEr.Service.Product;
import az.CartEr.Service.UploadImageService;
import az.CartEr.Service.UserService;
import az.CartEr.repository.UserRepository;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController

public class MessageController {

    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;


    @Autowired
    private Product noteService;

    @GetMapping("/users/product/")
    public List<ProductModel> getNotesByUser(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            if (tokenManager.tokenValidate(token)) {
                String email = tokenManager.getUserByToken(token);
                Optional<User> user = userRepository.findByEmail(email);
                return noteService.getNotesByUser(user);
            } else {
                System.out.println("Invalid token.");
                return Collections.emptyList();
            }

        } else {
            System.out.println("Authorization header must be provided with a Bearer token.");
            return Collections.emptyList();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageSaveController.class);

    @Autowired
    private UploadImageService fileService;

    @PostMapping("/users/addproduct")
    public ResponseEntity<ProductDTO> createNote(
            @RequestHeader("Authorization") String bearerToken,
            @RequestPart("noteCreateRequest") ProductDTO noteCreateRequest,
            @RequestPart("file") MultipartFile multipartFile
    ) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        logger.info("HIT -/upload | File Name : {}", multipartFile.getOriginalFilename());

        String imageUrl;
        try {
            imageUrl = (String) fileService.upload(multipartFile);
        } catch (Exception e) {
            logger.error("File upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            if (tokenManager.tokenValidate(token)) {
                String email = tokenManager.getUserByToken(token);

                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    ProductModel
                            note = new ProductModel();

                    note.setTitle(noteCreateRequest.getTitle());
                    note.setDescription(noteCreateRequest.getDescription());
                    note.setCategory(noteCreateRequest.getCategory());
                    note.setPicture(imageUrl);
                    note.setLocation(noteCreateRequest.getLocation());
                    note.setPrice(noteCreateRequest.getPrice());
                    note.setType(noteCreateRequest.getType());
                    ProductModel createdNote = noteService.createNoteForUser(note, user);

                    ProductDTO noteDTO = new ProductDTO(
                            createdNote.getId(),
                            createdNote.getDescription(),
                            createdNote.getTitle(),
                            createdNote.getCategory(),
                            createdNote.getPicture(),
                            createdNote.getLocation(),
                            createdNote.getPrice(),
                            createdNote.getType()
                    );

                    return ResponseEntity.status(HttpStatus.CREATED).body(noteDTO);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required request part: " + ex.getRequestPartName());
    }


    @DeleteMapping("/users/product/{id}")
    public ResponseEntity<String> deleteNote(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Long id
    ) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            if (tokenManager.tokenValidate(token)) {
                String email = tokenManager.getUserByToken(token);

                boolean isDeleted = noteService.deleteNoteById(id, email);
                if (isDeleted) {
                    return ResponseEntity.ok("Note deleted successfully.");
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Note not found.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization header must be provided with a Bearer token.");
        }


    }

    @PutMapping("users/product/{id}")
    public ResponseEntity<ProductModel> editNote(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Long id,
            @RequestPart("updateproduct") ProductDTO noteDTO,
            @RequestPart(value = "file", required = false) MultipartFile multipartFile
    ) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            if (tokenManager.tokenValidate(token)) {
                String email = tokenManager.getUserByToken(token);

                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    List<ProductModel> userNotes = noteService.getNotesByUser(Optional.of(user));
                    Optional<ProductModel> existingNoteOptional = userNotes.stream()
                            .filter(note -> note.getId().equals(id))
                            .findFirst();

                    if (existingNoteOptional.isPresent()) {
                        ProductModel existingNote = existingNoteOptional.get();

                        // If a new file is provided, update the picture
                        if (multipartFile != null && !multipartFile.isEmpty()) {
                            try {
                                String imageUrl = (String) fileService.upload(multipartFile);
                                noteDTO.setPicture(imageUrl);
                            } catch (Exception e) {
                                logger.error("File upload failed", e);
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                            }
                        } else {
                            // Preserve the existing picture URL if no new file is provided
                            noteDTO.setPicture(existingNote.getPicture());
                        }

                        ProductModel updatedNote = noteService.updateNoteById(id, noteDTO, email);
                        if (updatedNote != null) {
                            return ResponseEntity.ok(updatedNote);
                        } else {
                            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                        }
                    } else {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Autowired
    private Product productService;

    @GetMapping("user/product/{id}")
    public ResponseEntity<String> getProductById(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable int id) throws IOException {

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            if (tokenManager.tokenValidate(token)) {
                String email = tokenManager.getUserByToken(token);

                Optional<ProductModel> productOptional = productService.getProductById(id);
                if (productOptional.isPresent()) {
                    GetAllProductDto productDTO = convertToProductDTO(productOptional.get());

                    String emailBody = "<!DOCTYPE html>\n" +
                            "<html lang=\"en\">\n" +
                            "<head>\n" +
                            "    <meta charset=\"UTF-8\">\n" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                            "    <title>Product Information Request</title>\n" +
                            "    <style>\n" +
                            "        body {\n" +
                            "            font-family: Arial, sans-serif;\n" +
                            "            background-color: #f4f4f4;\n" +
                            "            margin: 0;\n" +
                            "            padding: 0;\n" +
                            "        }\n" +
                            "        .container {\n" +
                            "            background-color: #ffffff;\n" +
                            "            margin: 50px auto;\n" +
                            "            padding: 20px;\n" +
                            "            max-width: 600px;\n" +
                            "            border-radius: 8px;\n" +
                            "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n" +
                            "        }\n" +
                            "        .header {\n" +
                            "            text-align: center;\n" +
                            "            padding-bottom: 20px;\n" +
                            "        }\n" +
                            "        .header h1 {\n" +
                            "            margin: 0;\n" +
                            "            color: #333333;\n" +
                            "        }\n" +
                            "        .content {\n" +
                            "            padding: 20px;\n" +
                            "            border-top: 1px solid #eeeeee;\n" +
                            "        }\n" +
                            "        .content p {\n" +
                            "            line-height: 1.6;\n" +
                            "            color: #333333;\n" +
                            "        }\n" +
                            "        .content .product-details {\n" +
                            "            margin: 20px 0;\n" +
                            "            padding: 20px;\n" +
                            "            background-color: #f9f9f9;\n" +
                            "            border: 1px solid #dddddd;\n" +
                            "            border-radius: 8px;\n" +
                            "        }\n" +
                            "        .content .product-details img {\n" +
                            "            max-width: 100%;\n" +
                            "            height: auto;\n" +
                            "            display: block;\n" +
                            "            margin: 0 auto 10px;\n" +
                            "        }\n" +
                            "        .content .product-details h2 {\n" +
                            "            margin: 0;\n" +
                            "            color: #555555;\n" +
                            "        }\n" +
                            "        .content .product-details p {\n" +
                            "            margin: 5px 0;\n" +
                            "            color: #777777;\n" +
                            "        }\n" +
                            "        .footer {\n" +
                            "            text-align: center;\n" +
                            "            padding-top: 20px;\n" +
                            "            font-size: 0.9em;\n" +
                            "            color: #aaaaaa;\n" +
                            "        }\n" +
                            "    </style>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "    <div class=\"container\">\n" +
                            "        <div class=\"header\">\n" +
                            "            <h1>Product Information Request</h1>\n" +
                            "        </div>\n" +
                            "        <div class=\"content\">\n" +
                            "            <p>Dear " + productDTO.getUserEmail() + ",</p>\n" +
                            "            <p>I would like to get this product:</p>\n" +
                            "            <div class=\"product-details\">\n" +
                            "                <img src=\"" + productDTO.getPicture() + "\" alt=\"Product Image\">\n" +
                            "                <h2>" + productDTO.getTitle() + "</h2>\n" +
                            "                <p><strong>Description:</strong> " + productDTO.getDescription() + "</p>\n" +
                            "                <p><strong>Category:</strong> " + productDTO.getCategory() + "</p>\n" +
                            "                <p><strong>Location:</strong> " + productDTO.getLocation() + "</p>\n" +
                            "                <p><strong>Price:</strong> " + productDTO.getPrice() + "</p>\n" +
                            "                <p><strong>Type:</strong> " + productDTO.getType() + "</p>\n" +
                            "            </div>\n" +
                            "            <p>If you need further details, please do not hesitate to contact us.</p>\n" +
                            "            <p>Best regards,<br>" + email + "</p>\n" +
                            "        </div>\n" +
                            "        <div class=\"footer\">\n" +
                            "            <p>&copy; 2024 Carter. All rights reserved.</p>\n" +
                            "        </div>\n" +
                            "    </div>\n" +
                            "</body>\n" +
                            "</html>";

                    String subject = "Request a product name " + productDTO.getTitle().toString();
                    String escapedEmailBody = StringEscapeUtils.escapeJson(emailBody);

                    // Create JSON payload
                    String jsonPayload = String.format("{\"email\":\"%s\", \"ccemail\":\"%s\", \"subject\":\"%s\", \"message\":\"%s\"}",
                            productDTO.getUserEmail(), email, subject, escapedEmailBody);

                    URL url = new URL("http://abidjavaapp.alwaysdata.net/MailApi/mailservis.php");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setDoOutput(true);

                    // Write the JSON data to the request
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonPayload.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    // Get the response
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the response body
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            System.out.println("Email sent successfully! Response: " + response.toString());
                            return ResponseEntity.ok("Email sent successfully! Response: " + response.toString());
                        }
                    } else {
                        // Read the error response body
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                            StringBuilder response = new StringBuilder();
                            String responseLine = null;
                            while ((responseLine = br.readLine()) != null) {
                                response.append(responseLine.trim());
                            }
                            System.out.println("Failed to send email. Response code: " + responseCode + ". Response body: " + response.toString());
                            return ResponseEntity.status(responseCode).body("Failed to send email. Response code: " + responseCode + ". Response body: " + response.toString());
                        }
                    }
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.status(HttpURLConnection.HTTP_UNAUTHORIZED).body("Invalid token");
        }
    }


    private GetAllProductDto convertToProductDTO(ProductModel product) {
        GetAllProductDto productDTO = new GetAllProductDto();
        productDTO.setId(product.getId());
        productDTO.setTitle(product.getTitle());
        productDTO.setDescription(product.getDescription());
        productDTO.setPicture(product.getPicture());
        productDTO.setCategory(product.getCategory());
        productDTO.setPrice(product.getPrice());
        productDTO.setLocation(product.getLocation());
        productDTO.setType(product.getType());
        if (product.getUser() != null) {
            productDTO.setUserEmail(product.getUser().getEmail());
        }
        return productDTO;
    }




}
