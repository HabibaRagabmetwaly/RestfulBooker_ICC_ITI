package services;

import base.BaseTest;
import io.restassured.response.Response;
import models.Booking;
import models.BookingDates;
import models.BookingResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class BookingService extends BaseTest {
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    public BookingResponse createBooking(Booking booking) {
        logger.info("Creating booking for: {} {}", booking.getFirstname(), booking.getLastname());

        Response response = given()
                .spec(requestSpec)
                .body(booking)
                .when()
                .post("/booking");

        return response.as(BookingResponse.class);
    }

    public Response createBookingRawResponse(Booking booking) {
        logger.info("Creating booking with raw response");

        return given()
                .spec(requestSpec)
                .body(booking)
                .when()
                .post("/booking");
    }

    public Response getBookingById(Integer id) {
        logger.info("Getting booking by ID: {}", id);

        return given()
                .spec(requestSpec)
                .when()
                .get("/booking/" + id);
    }

    public Response getBookings() {
        logger.info("Getting all bookings");

        return given()
                .spec(requestSpec)
                .when()
                .get("/booking");
    }

    public Response getBookingsWithFilters(Map<String, String> queryParams) {
        logger.info("Getting bookings with filters: {}", queryParams);

        return given()
                .spec(requestSpec)
                .queryParams(queryParams)
                .when()
                .get("/booking");
    }

    public Response updateBooking(Integer id, Booking booking, String token) {
        logger.info("Updating booking ID: {}", id);

        return given()
                .spec(requestSpec)
                .cookie("token", token)
                .body(booking)
                .when()
                .put("/booking/" + id);
    }

    public Response partialUpdateBooking(Integer id, Map<String, Object> fields, String token) {
        logger.info("Partially updating booking ID: {}", id);

        return given()
                .spec(requestSpec)
                .cookie("token", token)
                .body(fields)
                .when()
                .patch("/booking/" + id);
    }

    public Response deleteBooking(Integer id, String token) {
        logger.info("Deleting booking ID: {}", id);

        return given()
                .spec(requestSpec)
                .cookie("token", token)
                .when()
                .delete("/booking/" + id);
    }

    // Helper method to create sample booking
    public static Booking createSampleBooking() {
        BookingDates bookingDates = new BookingDates("2024-01-01", "2024-01-05");
        Booking booking = new Booking();
        booking.setFirstname("Jim");
        booking.setLastname("Brown");
        booking.setTotalprice(111);
        booking.setDepositpaid(true);
        booking.setBookingdates(bookingDates);
        booking.setAdditionalneeds("Breakfast");
        return booking;
    }

    // Helper method to create minimal booking
    public static Booking createMinimalBooking() {
        Booking booking = new Booking();
        booking.setFirstname("Park");
        booking.setLastname("pogumm");
        return booking;
    }
}