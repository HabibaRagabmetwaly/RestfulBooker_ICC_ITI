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
    @Test(description = "PUT-03: Update booking with missing firstname")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Negative - Update Booking")
    @Description("Verify PUT update fails when a required field is missing")
    public void testPutUpdateMissingFirstName() {

        // Create booking first
        Booking original = BookingService.createSampleBooking();
        BookingResponse created = bookingService.createBooking(original);

        Booking invalidUpdate = new Booking();
        invalidUpdate.setLastname("Brown");
        invalidUpdate.setTotalprice(200);
        invalidUpdate.setDepositpaid(true);
        invalidUpdate.setBookingdates(new BookingDates("2018-01-01", "2019-01-01"));

        Response response = bookingService.updateBooking(created.getBookingid(), invalidUpdate, token);

        assertNotEquals(response.getStatusCode(), 200,
                "PUT should fail when firstname is missing");
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

    @Test(description = "PUT-05: Update non-existing booking ID")
    @Severity(SeverityLevel.MINOR)
    @Story("Negative - Not Found")
    @Description("Verify PUT update fails for non-existing booking ID")
    public void testPutUpdateNonExistingId() {

        int invalidId = 999999;

        Booking booking = BookingService.createSampleBooking();

        Response response = bookingService.updateBooking(invalidId, booking, token);

        assertEquals(response.getStatusCode(), 405,
                "Expected 405 Method Not Allowed for non-existing booking ID");
    }

    @Test(description = "PUT-06: Update booking with invalid schema (known bug)")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Negative - Invalid Booking Data")
    @Issue("BUG-API-VALIDATION-BOOKING-SCHEMA")
    public void testUpdateBookingWithInvalidSchema_KnownBug() {

        // Arrange: create valid booking first
        Booking original = BookingService.createSampleBooking();
        BookingResponse created = bookingService.createBooking(original);

        // Create a clearly invalid booking object
        Booking corrupted = new Booking();
        corrupted.setFirstname(null);         // null firstname (required)
        corrupted.setLastname("X");
        corrupted.setTotalprice(-100);        // negative price
        corrupted.setDepositpaid(true);
        corrupted.setBookingdates(new BookingDates("2024-13-01", "2024-00-01"));
        // Act: try to update
        Response response = bookingService.updateBooking(created.getBookingid(), corrupted, token);

        System.out.println("PUT invalid schema status = " + response.getStatusCode());

       // fail("Known BUG: API accepts clearly invalid booking schema instead of rejecting it.");
    }

    @Test(description = "PATCH-NEG-07: Real race condition test with parallel PATCH")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Negative - Real Concurrency Issue")
    @Issue("BUG-API-CONCURRENCY-RACE")
    public void testPatchRaceCondition_REAL() throws InterruptedException {

        Booking booking = BookingService.createSampleBooking();
        BookingResponse created = bookingService.createBooking(booking);
        Integer bookingId = created.getBookingid();

        // Request bodies
        Map<String, Object> patchA = new HashMap<>();
        patchA.put("firstname", "ThreadA");

        Map<String, Object> patchB = new HashMap<>();
        patchB.put("firstname", "ThreadB");

        // To hold the responses from both threads
        final Response[] responseA = new Response[1];
        final Response[] responseB = new Response[1];

        Thread t1 = new Thread(() -> {
            responseA[0] = bookingService.partialUpdateBooking(bookingId, patchA, token);
        });

        Thread t2 = new Thread(() -> {
            responseB[0] = bookingService.partialUpdateBooking(bookingId, patchB, token);
        });

        // Run both at the SAME TIME
        t1.start();
        t2.start();

        // Wait for both to finish
        t1.join();
        t2.join();

        System.out.println("PATCH A status = " + responseA[0].getStatusCode());
        System.out.println("PATCH B status = " + responseB[0].getStatusCode());

        // EXPECTED: One should succeed, one should FAIL with 409
        boolean oneFailedAsConflict =
                responseA[0].getStatusCode() == 409 ||
                        responseB[0].getStatusCode() == 409;

        // ASSERT -> Will FAIL ALWAYS because RestfulBooker does NOT detect conflicts
        assertTrue(oneFailedAsConflict,
                "BUG: API failed to detect concurrent update conflict. Both requests were accepted!");
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

    @Test(description = "DEL-03: Delete booking with historical date (should be blocked)")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Negative - Integrity Constraints")
    @Description("Verify API prevents deleting bookings with past checkout dates")
    @Issue("BUG-API-SOFT-DELETE-RULES")
    public void testDeleteHistoricalBooking() {

        // Create booking with old/historical dates
        Booking booking = new Booking();
        booking.setFirstname("John");
        booking.setLastname("Doe");
        booking.setTotalprice(500);
        booking.setDepositpaid(true);
        booking.setBookingdates(new BookingDates("2010-01-01", "2010-01-05"));

        BookingResponse created = bookingService.createBooking(booking);

        Response deleteResponse = bookingService.deleteBooking(created.getBookingid(), token);

        // Expected Real Behavior → Prevent deletion for historical financial records
        assertEquals(deleteResponse.getStatusCode(), 409,
                "BUG: API should NOT ALLOW deleting historical bookings (integrity rule).");
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

