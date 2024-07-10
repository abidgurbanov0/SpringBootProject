package az.CartEr.Controller;

import az.CartEr.Auth.TokenManager;
import az.CartEr.DTO.LoginRequest;
import az.CartEr.DTO.OtpVerificationRequest;
import az.CartEr.DTO.UpdateUserRequest;
import az.CartEr.DTO.UserRegister;
import az.CartEr.Model.User;
import az.CartEr.Model.UserOtpPair;
import az.CartEr.Service.TempUserStorageService;
import az.CartEr.Service.UpdateUserServis;
import az.CartEr.Service.UploadImageService;
import az.CartEr.Service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequiredArgsConstructor
public class AuthController {
    private final TokenManager tokenManager;
    private final AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @RequestMapping("/login")
    @PostMapping
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {

       try {
           authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

       }
       catch(Exception e)
       {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect  password");
       }

       return ResponseEntity.ok(tokenManager.generate(loginRequest.getEmail()));
    }

    @Autowired
    private UserService userService;
    @Autowired
    private UpdateUserServis updateUserServis;
    @Autowired
private TempUserStorageService tempUserStorageService;

    private static final Logger logger = LoggerFactory.getLogger(ImageSaveController.class);

    @Autowired
    private UploadImageService fileService;
    @PostMapping("/registration")


    public ResponseEntity<String> registration(@RequestPart("AddContent") UserRegister userRequest,
                                               @RequestPart("File") MultipartFile multipartFile

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

        if (userService.isEmailAlreadyTaken(userRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        } else {

            // Save user details temporarily
            tempUserStorageService.removeUser(userRequest.getEmail());
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://abidjavaapp.alwaysdata.net/MailApi/mailotp.php?mail=" + userRequest.getEmail() ;
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String responseBody = response.getBody();
            List<String> otp1 = extractNumbers(responseBody);
            tempUserStorageService.saveUser(userRequest.getEmail(), new User(
                    userRequest.getName(), userRequest.getSurname(),
                    userRequest.getEmail(), passwordEncoder.encode(userRequest.getPassword()),imageUrl
            ), String.valueOf(otp1));




            return ResponseEntity.ok("User registered successfully. Check your email for OTP.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationRequest otpRequest) {
        String email = otpRequest.getEmail();
        String otp = otpRequest.getOtp();

        UserOtpPair tempUser1 = tempUserStorageService.getUserAndOtp(email);
        if (tempUser1 != null) {


            var otp11 = tempUser1.getOtp();
            String otp1 = otp11.replace("[", "").replace("]", "");
            User tempUser = tempUser1.getUser();
            if (otp1.toString().equals( otp.toString() )) {
                userService.createUser(tempUser.getName(), tempUser.getSurname(), tempUser.getEmail(),(tempUser.getPassword()),tempUser.getPPUrl());
                tempUserStorageService.removeUser(email);
                return ResponseEntity.ok("Registration successful");
            } else {

             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");

            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Temporary user data not found");
        }

    }
    @PutMapping("/editUser")
    public ResponseEntity<String> editUser(
            @RequestHeader("Authorization") String token,
            @RequestPart("User") UpdateUserRequest userRequest,
            @RequestPart(value = "File", required = false) MultipartFile multipartFile) {

        String jwtToken = token.replace("Bearer ", "");
        Claims claims = tokenManager.parseToken(jwtToken);
        String emailFromToken = claims.getSubject();

        String imageUrl = null;
        if (multipartFile != null && !multipartFile.isEmpty()) {
            try {
                imageUrl = (String) fileService.upload(multipartFile);
            } catch (Exception e) {
                logger.error("File upload failed", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
            }
        }

        try {
            updateUserServis.updateUser(emailFromToken, userRequest.getOldPassword(), userRequest.getName(), userRequest.getSurname(), userRequest.getPassword(), imageUrl);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Incorrect old password")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect old password");
            }
            throw e;
        }

        return ResponseEntity.ok("User updated successfully");
    }

    private List<String> extractNumbers(String text) {
        List<String> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required request part: " + ex.getRequestPartName());
    }
}




