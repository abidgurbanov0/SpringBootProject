package az.CartEr.Model;

public class UserOtpPair {
    private User user;
    private String otp;

    public UserOtpPair(User user, String otp) {
        this.user = user;
        this.otp = otp;
    }

    public User getUser() {
        return user;
    }

    public String getOtp() {
        return otp;
    }
}
