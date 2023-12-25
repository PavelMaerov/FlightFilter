package com.gridnine.testing;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;

import static com.gridnine.testing.FlightUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class FlightUtilsTest {
    LocalDateTime now = LocalDateTime.now();
    Segment segment1 = new Segment(now.plusDays(1), now.plusDays(2));
    Segment segment2 = new Segment(now.plusDays(3), now.plusDays(4));
    Segment corruptedSegment = new Segment(now.plusDays(6), now.plusDays(5));

    ExtendedFlight flight = new ExtendedFlight(List.of(segment1, segment2));
    ExtendedFlight corruptedFlight = new ExtendedFlight(List.of(segment2, corruptedSegment));
    List<ExtendedFlight> flights = List.of(flight, corruptedFlight);
    @Test
    void singleFilterTest() {
        List<ExtendedFlight> expectedFlights = List.of(corruptedFlight);
        List<ExtendedFlight> actualFlights = filter(flights,
                flight -> flight.departureDate().isAfter(now.plusDays(2)));
        assertEquals(expectedFlights, actualFlights);
    }
    @Test
    void multiFilterTest() {
        List<Predicate<ExtendedFlight>> filters = List.of(
                flight -> flight.departureDate().isAfter(now.plusDays(2)),
                ExtendedFlight::arrivalAfterDeparture);

        List<ExtendedFlight> expectedFlights = Collections.EMPTY_LIST;
        List<ExtendedFlight> actualFlights = filter(flights, filters);
        assertEquals(expectedFlights, actualFlights);
    }
    @Test
    void createTreeMapTest() {
        TreeMap<Long, ExtendedFlight> map = createTreeMap(flights,ExtendedFlight::departureDateKey);
        //первый полет (отправление через 1 день) ляжет с головы,
        //второй(поврежденный, отправление через 3 дня) - с хвоста

        //Попросим те полеты, что отправляются до 2х дней вперед
        SortedMap<Long, ExtendedFlight> filteredMap = map.headMap(dateToKey(now.plusDays(2)));
        //должны получить один элемент в мапе со значением первого полета в списке
        //и ключом от даты now.plusDays(1) плюс номер счетчика 1, т.к. этот полет был первым в списке

        assertEquals(1, filteredMap.size());
        assertEquals(dateToKey(now.plusDays(1))+1, filteredMap.firstKey());
        assertEquals(flight, filteredMap.get(filteredMap.firstKey()));

        //Попросим те полеты, что отправляются после 2х дней вперед
        filteredMap = map.tailMap(dateToKey(now.plusDays(2)));
        //должны получить один элемент в мапе со значением второго полета в списке
        //и ключом от даты now.plusDays(3) плюс номер счетчика 2, т.к. этот полет был вторым в списке

        assertEquals(1, filteredMap.size());
        assertEquals(dateToKey(now.plusDays(3))+2, filteredMap.firstKey());
        assertEquals(corruptedFlight, filteredMap.get(filteredMap.firstKey()));
    }
}