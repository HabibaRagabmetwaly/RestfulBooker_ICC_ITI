package services;

import base.BaseTest;
import io.restassured.response.Response;
import models.AuthRequest;
import models.AuthResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.restassured.RestAssured.given;

public class AuthService extends BaseTest {
    private static final Logger logger = LogManager.getLogger(AuthService.class);

    public AuthResponse createToken(String username, String password) {
        logger.info("Creating token for user: {}", username);

        AuthRequest authRequest = new AuthRequest(username, password);

        Response response = given()
                .spec(requestSpec)
                .body(authRequest)
                .when()
                .post("/auth");

        return response.as(AuthResponse.class);
    }

    public Response createTokenRawResponse(String username, String password) {
        logger.info("Creating token with raw response for user: {}", username);

        AuthRequest authRequest = new AuthRequest(username, password);

        return given()
                .spec(requestSpec)
                .body(authRequest)
                .when()
                .post("/auth");
    }
}