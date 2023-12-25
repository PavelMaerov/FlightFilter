package com.gridnine.testing;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static com.gridnine.testing.FlightUtils.dateToKey;
import static com.gridnine.testing.FlightUtils.IntegerToKey;

/**
 * Дополняет класс Flight расчетом разных интегральных характеристик
 * Этот класс можно расширять по мере возникновения новых характеристик.
 * Эти характеристики позволяют создавать объекты-предикаты для класса полета
 */
public class ExtendedFlight extends Flight {
    ExtendedFlight(final List<Segment> segs) {super(segs);}

    /**
     * Предполагая, что сегменты отсортированы по порядку
     * этот метод возвращает время вылета для первого сегмента полета
     * @return LocalDateTime
     */
    public LocalDateTime departureDate() {
        return getSegments().get(0).getDepartureDate();
    }

    /**
     * Возвращает false, если в полете есть, хотя бы один некорректнвый сегмент,
     * где время вылета больше чем время прилета
     * Если во всех сегментах прибытие после вылета, то - true
     * @return boolean
     */
    public boolean arrivalAfterDeparture() {
        for (Segment segment : getSegments()) {
            if (segment.getDepartureDate().isAfter(segment.getArrivalDate())) return false;
        }
        return true;
    }

    /**
     * Общее время пересадок в минутах
     * Некорректные сегменты, где прилет рашьше вылета
     * и сегменты, образующие отрицательное время пересадки - исключаются из перебора
     * @return int
     */
    public int groundTime() { //в минутах
        long sumGroundTime = 0; //в секундах
        long previousArrivalDate = 0; //в секундах
        ZoneOffset offset = ZoneOffset.UTC;
        for (Segment segment : getSegments()) {
            long DepartureDate = segment.getDepartureDate().toEpochSecond(offset);
            long ArrivalDate = segment.getArrivalDate().toEpochSecond(offset);
            //если не первый сегмент и сегмент корректный и время на земле - не отрицательно
            if ((previousArrivalDate > 0) && (ArrivalDate > DepartureDate) && (DepartureDate > previousArrivalDate)) {
                sumGroundTime += DepartureDate - previousArrivalDate;
            }
            previousArrivalDate = ArrivalDate;
        }
        return (int)sumGroundTime/60;
    }

    //Ключи для построения деревьев TreeMap, вычисляемые для каждого полета
    /**
     * @return возвращает ключ дерева для даты отправления полета
     */
    public long departureDateKey() {
        return dateToKey(departureDate());
    }

    //строить ключ для неправильных полетов не стал,
    //чтобы не утомлять читателя преобразованием boolean в integer

    /**
     * @return возвращает ключ для времени пересадки для записи полета в карту
     */
    public long groundTimeKey() {
        return IntegerToKey(groundTime());
    }
}
