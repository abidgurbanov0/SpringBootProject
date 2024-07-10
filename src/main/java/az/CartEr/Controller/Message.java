package az.CartEr.Controller;

import az.CartEr.Auth.TokenManager;
import az.CartEr.Model.User;
import az.CartEr.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class Message {

    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("message")
    public String getMessage(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);

            if (tokenManager.tokenValidate(token)) {
                String email = tokenManager.getUserByToken(token);

                // Extract the User object from the Optional
                Optional<User> optionalUser = userRepository.findByEmail(email);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();

                    // Create a JSON object
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode userJson = mapper.createObjectNode();
                    userJson.put("name", user.getName());
                    userJson.put("surname", user.getSurname());
                    userJson.put("email", user.getEmail());
                    userJson.put("ppUrl", user.getPPUrl());
                    userJson.put("message", "Your token is valid.");

                    return userJson.toString();
                } else {
                    return "User not found!";
                }

            } else {
                return "Invalid or expired token.";
            }
        } else {
            return "Authorization header must be provided with a Bearer token.";
        }
    }
}
