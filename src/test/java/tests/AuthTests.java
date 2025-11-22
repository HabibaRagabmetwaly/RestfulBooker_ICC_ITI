package tests;

import base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.AuthResponse;
import org.testng.annotations.Test;
import services.AuthService;

import static org.testng.Assert.*;

@Epic("Authentication API")
@Feature("Auth Operations")
public class AuthTests extends BaseTest {
    private AuthService authService = new AuthService();

    @Test(description = "TC-AUTH-01: Login with valid credentials")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Happy Path - Valid Login")
    @Description("Verify successful login with valid credentials")
    public void testLoginWithValidCredentials() {
        AuthResponse authResponse = authService.createToken("admin", "password123");

        assertNotNull(authResponse.getToken(), "Token should not be null");
        assertTrue(authResponse.getToken().length() > 0, "Token should not be empty");

        // Save token for other tests
        token = authResponse.getToken();
        logger.info("Successfully obtained token: {}", token.substring(0, 10) + "...");
    }
//bad test case
    @Test(description = "TC-AUTH-02: Login with invalid credentials")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Invalid Login")
    @Description("Verify login fails with invalid credentials")
    public void testLoginWithInvalidCredentials() {
        AuthResponse authResponse = authService.createToken("invalid", "wrong");

        assertNull(authResponse.getToken(), "Token should be null for invalid credentials");
        assertNotNull(authResponse.getReason(), "Reason should be provided");
        assertEquals(authResponse.getReason(), "Bad credentials");
    }

    @Test(description = "TC-AUTH-03: Login without password")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Missing Password")
    @Description("Verify login fails when password is missing")
    public void testLoginWithoutPassword() {
        Response response = authService.createTokenRawResponse("admin", null);

        assertEquals(response.getStatusCode(), 200);
        AuthResponse authResponse = response.as(AuthResponse.class);
        assertNull(authResponse.getToken());
        assertEquals(authResponse.getReason(), "Bad credentials");
    }

    @Test(description = "TC-AUTH-04: Login with empty data")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Empty Data")
    @Description("Verify login fails with empty request data")
    public void testLoginWithEmptyData() {
        Response response = authService.createTokenRawResponse(null, null);

        assertEquals(response.getStatusCode(), 200);
        AuthResponse authResponse = response.as(AuthResponse.class);
        assertNull(authResponse.getToken());
        assertEquals(authResponse.getReason(), "Bad credentials");
    }

    @Test(description = "TC-AUTH-05: Login SQL Injection")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Security - SQL Injection")
    @Description("Verify SQL injection attempt is handled properly")
    public void testLoginWithSQLInjection() {
        Response response = authService.createTokenRawResponse("admin' OR '1'='1", "any");

        assertEquals(response.getStatusCode(), 200);
        AuthResponse authResponse = response.as(AuthResponse.class);
        assertNull(authResponse.getToken());
        assertEquals(authResponse.getReason(), "Bad credentials");
    }
}