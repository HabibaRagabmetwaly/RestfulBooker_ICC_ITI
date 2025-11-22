package tests;

import base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.Booking;
import models.BookingResponse;
import org.testng.annotations.Test;
import services.AuthService;
import services.BookingService;
import models.BookingResponse;
import models.BookingDates;
import static org.testng.Assert.*;

@Epic("Booking API")
@Feature("End-to-End Booking Flow")
public class BookingE2ETest extends BaseTest {
    private BookingService bookingService = new BookingService();
    private AuthService authService = new AuthService();

    private Integer e2eBookingId;
    private String e2eToken;

    @Test(description = "E2E: Complete booking lifecycle")
    @Severity(SeverityLevel.CRITICAL)
    @Story("End-to-End Flow")
    @Description("Complete test of booking creation, retrieval, update and deletion")
    public void testCompleteBookingLifecycle() {
        // Step 1: Login and get token
        e2eToken = authService.createToken("admin", "password123").getToken();
        assertNotNull(e2eToken, "Token should be obtained successfully");
        logger.info("Step 1 - Login: Successfully obtained token");

        // Step 2: Create booking
        Booking booking = new Booking();
        booking.setFirstname("Jimy");
        booking.setLastname("Brownval");
        booking.setTotalprice(1111);
        booking.setDepositpaid(true);

        BookingDates dates = new BookingDates("2025-05-01", "2025-08-01");
        booking.setBookingdates(dates);
        booking.setAdditionalneeds("Breakfast");

        BookingResponse createResponse = bookingService.createBooking(booking);
        e2eBookingId = createResponse.getBookingid();
        assertNotNull(e2eBookingId, "Booking should be created successfully");
        logger.info("Step 2 - Create Booking: Successfully created booking with ID: {}", e2eBookingId);

        // Step 3: Get created booking
        Response getResponse = bookingService.getBookingById(e2eBookingId);
        assertEquals(getResponse.getStatusCode(), 200, "Should retrieve booking successfully");

        Booking retrievedBooking = getResponse.as(Booking.class);
        assertEquals(retrievedBooking.getFirstname(), "Jimy");
        assertEquals(retrievedBooking.getLastname(), "Brownval");
        logger.info("Step 3 - Get Booking: Successfully retrieved booking details");

        // Step 4: Update booking
        Booking updatedBooking = new Booking();
        updatedBooking.setFirstname("Mark");
        updatedBooking.setLastname("Smith");
        updatedBooking.setTotalprice(1000);
        updatedBooking.setDepositpaid(false);

        BookingDates updatedDates = new BookingDates("2024-05-12", "2025-01-11");
        updatedBooking.setBookingdates(updatedDates);

        Response updateResponse = bookingService.updateBooking(e2eBookingId, updatedBooking, e2eToken);
        assertEquals(updateResponse.getStatusCode(), 200, "Should update booking successfully");

        Booking updateResult = updateResponse.as(Booking.class);
        assertEquals(updateResult.getFirstname(), "Mark");
        assertEquals(updateResult.getLastname(), "Smith");
        logger.info("Step 4 - Update Booking: Successfully updated booking");

        // Step 5: Delete booking
        Response deleteResponse = bookingService.deleteBooking(e2eBookingId, e2eToken);
        assertEquals(deleteResponse.getStatusCode(), 201, "Should delete booking successfully");
        logger.info("Step 5 - Delete Booking: Successfully deleted booking");

        // Verify deletion
        Response verifyDeleteResponse = bookingService.getBookingById(e2eBookingId);
        assertEquals(verifyDeleteResponse.getStatusCode(), 404, "Booking should no longer exist");
        logger.info("E2E Test Completed: All steps executed successfully");
    }
}