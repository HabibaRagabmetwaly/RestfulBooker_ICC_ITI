package tests;

import base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.Booking;
import models.BookingDates;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import services.AuthService;
import services.BookingService;
import models.BookingResponse;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

@Epic("Booking API")
@Feature("Booking Operations - Negative Tests")
public class NegativeBookingTests extends BaseTest {
    private BookingService bookingService = new BookingService();
    private AuthService authService = new AuthService();

    @BeforeClass
    public void setupAuth() {
        token = authService.createToken("admin", "password123").getToken();
    }

    @Test(description = "TC-BOOK-01: Create booking without firstname")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Missing Required Field")
    @Description("Verify booking creation fails without firstname")
    public void testCreateBookingWithoutFirstname() {
        Booking booking = new Booking();
        booking.setLastname("Brown");
        booking.setTotalprice(111);
        booking.setDepositpaid(true);

        BookingDates dates = new BookingDates("2018-01-01", "2019-01-01");
        booking.setBookingdates(dates);
        booking.setAdditionalneeds("Breakfast");

        Response response = bookingService.createBookingRawResponse(booking);

        // API might return 500 or 400 for missing required fields
        assertTrue(response.getStatusCode() >= 400,
                "Should return error status for missing required field");
    }

    @Test(description = "TC-BOOK-02: Create booking with wrong total price type")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Invalid Data Type")
    @Description("Verify booking creation with various data types in totalprice field")
    public void testCreateBookingWithWrongTotalPriceType() {
        Booking booking = BookingService.createSampleBooking();

        Object[] invalidValues = {
                "one thousand",     // string
                true,              // boolean
                "123abc",          // alphanumeric
                "100.50",          // decimal string
                "0"                // zero as string
        };

        for (Object invalidValue : invalidValues) {
            booking.setTotalprice(invalidValue);

            Response response = bookingService.createBookingRawResponse(booking);

            System.out.println("Value: " + invalidValue + " → Status: " + response.getStatusCode());

            assertTrue(response.getStatusCode() < 500,
                    "API crashed with value: " + invalidValue);

            if (response.getStatusCode() == 200) {
                BookingResponse bookingResponse = response.as(BookingResponse.class);
                assertNotNull(bookingResponse.getBookingid(),
                        "Booking should be created with value: " + invalidValue);
            }
        }
    }

    @Test(description = "TC-BOOK-03: Create booking with invalid dates")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Invalid Dates")
    @Description("Verify booking creation with invalid date format")
    public void testCreateBookingWithInvalidDates() {
        Booking booking = new Booking();
        booking.setFirstname("Jim");
        booking.setLastname("Brown");
        booking.setTotalprice(111);
        booking.setDepositpaid(true);

        String[][] invalidDatePairs = {
                {"invalid-date", "invalid-date"},
                {"2024-13-01", "2024-12-01"},
                {"2024-02-30", "2024-03-01"},
                {"01-01-2024", "02-01-2024"},
                {"2024/01/01", "2024/01/02"}
        };

        for (String[] datePair : invalidDatePairs) {
            BookingDates dates = new BookingDates(datePair[0], datePair[1]);
            booking.setBookingdates(dates);

            Response response = bookingService.createBookingRawResponse(booking);

            System.out.println("Dates: " + datePair[0] + " / " + datePair[1] + " → Status: " + response.getStatusCode());

            assertTrue(response.getStatusCode() < 500,
                    "API crashed with invalid dates: " + datePair[0] + " / " + datePair[1]);
        }
    }

    @Test(description = "TC-BOOK-04: Create booking with XSS injection")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Security - XSS Injection")
    @Description("Verify XSS injection attempt is handled properly")
    public void testCreateBookingWithXSSInjection() {
        Booking booking = BookingService.createSampleBooking();

        String[] xssPayloads = {
                "<script>alert('xss')</script>",
                "John<script>alert(1)</script>",
                "<img src=x onerror=alert(1)>",
                "'; alert('xss'); //"
        };

        for (String xssPayload : xssPayloads) {
            booking.setFirstname(xssPayload);

            Response response = bookingService.createBookingRawResponse(booking);

            System.out.println("XSS Payload: " + xssPayload + " → Status: " + response.getStatusCode());

            assertTrue(response.getStatusCode() < 500,
                    "API crashed with XSS payload: " + xssPayload);

            if (response.getStatusCode() == 200) {
                BookingResponse bookingResponse = response.as(BookingResponse.class);
                assertNotNull(bookingResponse.getBookingid(),
                        "Booking should be created with XSS payload: " + xssPayload);
            }
        }
    }

    @Test(description = "GETID-01: Get booking with non-existent ID")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Non-existent Resource")
    @Description("Verify getting non-existent booking returns 404")
    public void testGetBookingWithNonExistentId() {

        Response response = bookingService.getBookingById(999999999);

        assertEquals(response.getStatusCode(), 404,
                "Should return 404 for non-existent booking");
    }

    @Test(description = "GETID-02: Get booking with invalid ID type")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Invalid ID Type")
    @Description("Verify framework handles invalid ID types")
    public void testGetBookingWithInvalidIdType() {

        logger.info("Invalid ID type test - framework level validation");


        Response response = bookingService.getBookingById(-1);
        assertTrue(response.getStatusCode() >= 400,
                "Should return error for invalid ID");
    }

    @Test(description = "PUT-01: Update booking without token")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Negative Path - Unauthorized Access")
    @Description("Verify updating booking without token returns 403")
    public void testUpdateBookingWithoutToken() {

        Booking originalBooking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(originalBooking);
        Integer testBookingId = createdBooking.getBookingid();

        Booking updateBooking = BookingService.createSampleBooking();
        updateBooking.setFirstname("UpdatedName");

        Response response = bookingService.updateBooking(testBookingId, updateBooking, null);
        assertTrue(response.getStatusCode() >= 400,
                "Should return error for unauthorized update. Got: " + response.getStatusCode());
    }

    @Test(description = "PUT-02: Update booking with invalid token")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Negative Path - Invalid Token")
    @Description("Verify updating booking with invalid token returns error")
    public void testUpdateBookingWithInvalidToken() {

        Booking originalBooking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(originalBooking);
        Integer testBookingId = createdBooking.getBookingid();

        Booking updateBooking = BookingService.createSampleBooking();
        updateBooking.setFirstname("UpdatedName");

        Response response = bookingService.updateBooking(testBookingId, updateBooking, "invalid-token-12345");

        assertTrue(response.getStatusCode() >= 400,
                "Should return error for invalid token. Got: " + response.getStatusCode());
    }

    @Test(description = "PUT-04: Update booking with negative price")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Invalid Data")
    @Description("Verify updating booking with negative price")
    public void testUpdateBookingWithNegativePrice() {
        // First create a booking
        Booking originalBooking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(originalBooking);
        Integer testBookingId = createdBooking.getBookingid();

        // Try to update with negative price
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("totalprice", -100);

        Response response = bookingService.partialUpdateBooking(testBookingId, updateFields, token);

        System.out.println("Negative price update → Status: " + response.getStatusCode());
        assertTrue(response.getStatusCode() < 500,
                "Should not crash with negative price");
        if (response.getStatusCode() == 200) {
            Booking updatedBooking = response.as(Booking.class);
            assertNotNull(updatedBooking.getFirstname(),
                    "Booking should be returned after update");
        }
    }
    @Test(description = "DEL-01: Delete booking without token")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Negative Path - Unauthorized Deletion")
    @Description("Verify deleting booking without token returns error")
    public void testDeleteBookingWithoutToken() {

        Booking booking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(booking);
        Integer testBookingId = createdBooking.getBookingid();

        Response response = bookingService.deleteBooking(testBookingId, null);

        // maybe the status code (404 or 403)
        assertTrue(response.getStatusCode() >= 400,
                "Should return error for unauthorized deletion. Got: " + response.getStatusCode());
    }

    @Test(description = "DEL-02: Delete booking with invalid token")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Negative Path - Invalid Token Deletion")
    @Description("Verify deleting booking with invalid token returns error")
    public void testDeleteBookingWithInvalidToken() {

        Booking booking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(booking);
        Integer testBookingId = createdBooking.getBookingid();

        Response response = bookingService.deleteBooking(testBookingId, "invalid-token-12345");


        assertTrue(response.getStatusCode() >= 400,
                "Should return error for invalid token. Got: " + response.getStatusCode());
    }

    @Test(description = "DEL-04: Delete booking twice")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Path - Double Deletion")
    @Description("Verify deleting same booking twice returns error")
    public void testDeleteBookingTwice() {
        // Create a booking to delete
        Booking booking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(booking);
        Integer testBookingId = createdBooking.getBookingid();

        // First deletion - should succeed
        Response firstResponse = bookingService.deleteBooking(testBookingId, token);
        assertTrue(firstResponse.getStatusCode() == 201 || firstResponse.getStatusCode() == 200,
                "First deletion should succeed. Got: " + firstResponse.getStatusCode());

        // Second deletion - should fail
        Response secondResponse = bookingService.deleteBooking(testBookingId, token);
        assertTrue(secondResponse.getStatusCode() >= 400,
                "Should return error for already deleted booking. Got: " + secondResponse.getStatusCode());
    }
}

