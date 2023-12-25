package com.gridnine.testing;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ExtendedFlightTest {
    LocalDateTime now = LocalDateTime.now();
    Segment segment1 = new Segment(now.plusDays(1), now.plusDays(2));
    Segment segment2 = new Segment(now.plusDays(3), now.plusDays(4));
    Segment corruptedSegment = new Segment(now.plusDays(6), now.plusDays(5));

    ExtendedFlight flight = new ExtendedFlight(List.of(segment1, segment2));
    ExtendedFlight corruptedFlight = new ExtendedFlight(List.of(segment2, corruptedSegment));

    @Test
    void departureDateTest() {
        assertEquals(now.plusDays(1), flight.departureDate());
    }
    @Test
    void arrivalAfterDepartureTest() {
        assertTrue(flight.arrivalAfterDeparture());
        assertFalse(corruptedFlight.arrivalAfterDeparture());
    }
    @Test
    void groundTimeTest() {
        assertEquals(60*24, flight.groundTime());
        assertEquals(0, corruptedFlight.groundTime());
    }
}