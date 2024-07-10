package az.CartEr.Service;

import az.CartEr.Model.User;
import az.CartEr.Model.UserOtpPair;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TempUserStorageService {

    private Map<String, UserOtpPair> tempUserStorage = new HashMap<>();

    public void saveUser(String email, User user, String otp) {
        tempUserStorage.put(email, new UserOtpPair(user, otp));
    }

    public UserOtpPair getUserAndOtp(String email) {
        return tempUserStorage.get(email);
    }

    public void removeUser(String email) {
        tempUserStorage.remove(email);
    }
}
