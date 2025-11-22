package tests;
import base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import models.Booking;
import models.BookingResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import services.AuthService;
import services.BookingService;
import models.BookingDates;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

@Epic("Booking API")
@Feature("Booking Operations - Happy Path")
public class BookingTests extends BaseTest {
    private BookingService bookingService = new BookingService();
    private AuthService authService = new AuthService();

    @BeforeClass
    public void setupAuth() {
        // Get auth token before booking tests
        token = authService.createToken("admin", "password123").getToken();
        logger.info("Auth token setup completed");
    }

    @Test(description = "TC-BOOKING-01: Create booking with all data")
    @Severity(SeverityLevel.BLOCKER)
    @Story("Happy Path - Create Booking")
    @Description("Verify successful booking creation with all required data")
    public void testCreateBookingWithAllData() {
        Booking booking = BookingService.createSampleBooking();

        BookingResponse response = bookingService.createBooking(booking);

        assertNotNull(response.getBookingid(), "Booking ID should not be null");
        assertTrue(response.getBookingid() > 0, "Booking ID should be positive");

        // Verify booking details
        Booking createdBooking = response.getBooking();
        assertEquals(createdBooking.getFirstname(), booking.getFirstname());
        assertEquals(createdBooking.getLastname(), booking.getLastname());
        assertEquals(createdBooking.getTotalprice(), booking.getTotalprice());
        assertEquals(createdBooking.getDepositpaid(), booking.getDepositpaid());

        // Save booking ID for other tests
        bookingId = response.getBookingid();
        logger.info("Successfully created booking with ID: {}", bookingId);
    }

    @Test(description = "TC-BOOKING-02: Create booking with minimal data")
    @Severity(SeverityLevel.NORMAL)
    @Story("Happy Path - Minimal Data")
    @Description("Verify successful booking creation with minimal required data")
    public void testCreateBookingWithMinimalData() {

        Booking booking = new Booking();
        booking.setFirstname("Jim");
        booking.setLastname("Brown");
        booking.setTotalprice(111);
        booking.setDepositpaid(true);
        booking.setBookingdates(new BookingDates("2018-01-01", "2019-01-01"));

        BookingResponse response = bookingService.createBooking(booking);

        assertNotNull(response.getBookingid(), "Booking ID should not be null");
        assertTrue(response.getBookingid() > 0, "Booking ID should be positive");

        Booking createdBooking = response.getBooking();
        assertEquals(createdBooking.getFirstname(), booking.getFirstname());
        assertEquals(createdBooking.getLastname(), booking.getLastname());
    }

    @Test(description = "TC-GET-01: Get all bookings")
    @Severity(SeverityLevel.NORMAL)
    @Story("Happy Path - Get All Bookings")
    @Description("Verify retrieving all bookings works correctly")
    public void testGetAllBookings() {
        Response response = bookingService.getBookings();

        assertEquals(response.getStatusCode(), 200);
        assertTrue(response.getBody().asString().contains("bookingid"));

        // Parse response as array to verify structure
        Object[] bookings = response.as(Object[].class);
        assertTrue(bookings.length > 0, "Should return at least one booking");
    }

    @Test(description = "TC-GETID-01: Get booking by valid ID")
    @Severity(SeverityLevel.NORMAL)
    @Story("Happy Path - Get Booking by ID")
    @Description("Verify retrieving booking by valid ID works correctly")
    public void testGetBookingByValidId() {
        // First create a booking to ensure we have a valid ID
        Booking booking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(booking);
        Integer testBookingId = createdBooking.getBookingid();

        Response response = bookingService.getBookingById(testBookingId);

        assertEquals(response.getStatusCode(), 200);

        Booking retrievedBooking = response.as(Booking.class);
        assertEquals(retrievedBooking.getFirstname(), booking.getFirstname());
        assertEquals(retrievedBooking.getLastname(), booking.getLastname());
    }

    @Test(description = "TC-GETID-02: Get booking by name filter")
    @Severity(SeverityLevel.NORMAL)
    @Story("Happy Path - Filter Bookings")
    @Description("Verify filtering bookings by first and last name works correctly")
    public void testGetBookingByNameFilter() {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("firstname", "sally");
        queryParams.put("lastname", "brown");

        Response response = bookingService.getBookingsWithFilters(queryParams);

        assertEquals(response.getStatusCode(), 200);

        // Response should be an array of bookings
        Object[] bookings = response.as(Object[].class);
        assertNotNull(bookings);
    }

    @Test(description = "TC-PUT-01: Update booking with valid token")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Happy Path - Update Booking")
    @Description("Verify updating booking with valid token works correctly")
    public void testUpdateBookingWithValidToken() {
        // First create a booking
        Booking originalBooking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(originalBooking);
        Integer testBookingId = createdBooking.getBookingid();

        // Update the booking
        Booking updatedBooking = new Booking();
        updatedBooking.setFirstname("James");
        updatedBooking.setLastname("Brown");
        updatedBooking.setTotalprice(200);
        updatedBooking.setDepositpaid(false);

        BookingDates updatedDates = new BookingDates("2018-02-01", "2018-04-01");
        updatedBooking.setBookingdates(updatedDates);

        Response response = bookingService.updateBooking(testBookingId, updatedBooking, token);

        assertEquals(response.getStatusCode(), 200);

        Booking responseBooking = response.as(Booking.class);
        assertEquals(responseBooking.getFirstname(), "James");
        assertEquals(responseBooking.getLastname(), "Brown");
        assertEquals(responseBooking.getTotalprice(), 200);
    }

    @Test(description = "TC-DEL-01: Delete booking with valid token")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Happy Path - Delete Booking")
    @Description("Verify deleting booking with valid token works correctly")
    public void testDeleteBookingWithValidToken() {
        // First create a booking to delete
        Booking booking = BookingService.createSampleBooking();
        BookingResponse createdBooking = bookingService.createBooking(booking);
        Integer testBookingId = createdBooking.getBookingid();

        Response response = bookingService.deleteBooking(testBookingId, token);

        // Restful Booker returns 201 for successful deletion
        assertEquals(response.getStatusCode(), 201);

        // Verify booking is deleted by trying to get it
        Response getResponse = bookingService.getBookingById(testBookingId);
        assertEquals(getResponse.getStatusCode(), 404);
    }
}