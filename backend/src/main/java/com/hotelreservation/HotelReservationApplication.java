package com.hotelreservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Hotel Room Reservation System.
 * Bootstraps the Spring Boot application context.
 */
@SpringBootApplication
public class HotelReservationApplication {

    /**
     * Application main method.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(HotelReservationApplication.class, args);
    }
}
