package models;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse
{
    @JsonProperty("token")
    private String token;

    @JsonProperty("reason")
    private String reason;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
