package base;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
public class BaseTest
{
    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected static RequestSpecification requestSpec;
    protected static ResponseSpecification responseSpec;
    protected static PrintStream logStream;

    protected static String token;
    protected static Integer bookingId;

    @BeforeClass
    public static void setup() throws FileNotFoundException {
        // Setup logging to file
        File logFile = new File("logs/booking-api-tests.log");
        logFile.getParentFile().mkdirs();
        logStream = new PrintStream(logFile);

        // Base URI
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";

        // Common Request Specification
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter(logStream))
                .addFilter(new ResponseLoggingFilter(logStream))
                .addFilter(new AllureRestAssured())
                .build();

        // Common Response Specification
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();

        logger.info("Test setup completed");
    }

    @BeforeMethod
    public void methodSetup() {
        logger.info("Starting test method execution");
    }

    protected RequestSpecification getRequestSpec() {
        return RestAssured.given().spec(requestSpec);
    }

    protected RequestSpecification getRequestSpecWithAuth() {
        return getRequestSpec().cookie("token", token);
    }
}
