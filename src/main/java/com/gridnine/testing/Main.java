package com.gridnine.testing;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.gridnine.testing.FlightUtils.*;

public class Main {
    /**
    Стандартная печать списка выводит все полеты в нечитабельную строку
    Этот метод - для вывода списка с одним полетом в строке
     */
    private static void printFlights(Collection<ExtendedFlight> flights) {
        flights.forEach(System.out::println);
    }

    public static void main(String[] args) {
        //по заданию мне предоставили фабрику FlightBuilder, создающую список Flight
        List<Flight> initialFlights = FlightBuilder.createFlights();
        //Однако мне потребовалось оснастить класс Flight дополнительными методами и я создал класс ExtendedFlight
        //Можно было бы переделать фабрику так, чтобы она возвращала ExtendedFlight,
        //но я решил не трогать код задания и поэтому потребовалось сконвертировать список
        List<ExtendedFlight> flights = initialFlights.stream()
                .map(flight -> new ExtendedFlight(flight.getSegments()))
                .collect(Collectors.toList());

        System.out.println("Исходный набор полетов");
        printFlights(flights);

        System.out.println("Набор полетов после текущего момента");
        List<ExtendedFlight> flightsAfterNow = filter(flights,
                flight -> flight.departureDate().isAfter(LocalDateTime.now()));
        printFlights(flightsAfterNow);

        System.out.println("Набор полетов после исключения тех, что с некорректными сегментами");
        List<ExtendedFlight> correctFlights = filter(flights,
                ExtendedFlight::arrivalAfterDeparture);
        printFlights(correctFlights);

        System.out.println("Набор полетов после исключения тех, где время пересадок больше 2х часов");
        List<ExtendedFlight> quickFlights = filter(flights,
                flight -> flight.groundTime()<120);
        printFlights(quickFlights);

        System.out.println("Набор полетов после проверки всех условий");
        List<ExtendedFlight> resultFlights = filter(flights,
                flight -> flight.departureDate().isAfter(LocalDateTime.now()) &&
                flight.arrivalAfterDeparture() &&
                flight.groundTime()<120);
        printFlights(resultFlights);

        //Вариант с составлением списка условий, которых может быть очень много по заданию
        //В этом случае разные элементы списка условий могут быть предоставлены разными сервисами
        //
        //Это вариант для массива условий
        //Predicate<ExtendedFlight>[] filters = (Predicate<ExtendedFlight>[]) Array.newInstance(Predicate.class, 3);
        //Потом решил сделать список. Все-таки у списка есть готовые методы для вставки/удаления - вдруг потребуются
        List<Predicate<ExtendedFlight>> filters = List.of(
                flight -> flight.departureDate().isAfter(LocalDateTime.now()),
                ExtendedFlight::arrivalAfterDeparture,
                flight -> flight.groundTime()<120);

        System.out.println("Набор полетов после проверки всех условий из списка условий");
        List<ExtendedFlight> resultFlightsWithArrayOfConditions = filter(flights, filters);
        printFlights(resultFlightsWithArrayOfConditions);

        System.out.println("Набор полетов после фильтрации по индексу времени отправления");
        TreeMap<Long, ExtendedFlight> map = createTreeMap(flights,ExtendedFlight::departureDateKey);
        //дерево строим один раз, а ищем по нему многократно и быстро, используя tailMap, headMap, subMap
        SortedMap<Long, ExtendedFlight> filteredMap = map.tailMap(dateToKey(LocalDateTime.now()));
        printFlights(filteredMap.values());

        System.out.println("Набор полетов после фильтрации по индексу времени пересадки");
        map = createTreeMap(flights,ExtendedFlight::groundTimeKey);
        //дерево строим один раз, а ищем по нему многократно и быстро
        filteredMap = map.headMap(IntegerToKey(120));
        printFlights(filteredMap.values());

        System.out.println("Набор полетов после фильтрации по индексу времени пересадки и переборной фильтрации по остальным условиям");
        //передаем в filter не лист, а коллекцию. Но все равно этого достаточно для перебора
        resultFlights = filter(filteredMap.values(),
                List.of(
                flight -> flight.departureDate().isAfter(LocalDateTime.now()),
                ExtendedFlight::arrivalAfterDeparture));
        printFlights(resultFlights);

        //Сейчас в качестве единицы фильтрации использовался стандартный объект реализующий Predicate и наполняемый лямбдами
        //Можно и дальше развивать этот объект, добавляя к нему свойства "имеет индекс","селективность индекса",
        //"вероятность положительного результата". Это позволит выбрать правильный (наиболее селективный) индекс для первичного отбора
        //и расставить фильтры таким образом, чтобы вычисление по короткой схеме последовательности лигических "И" экономило наибольшее время
        //Все эти задачи давно уже решены разработчиками баз данных и, возможно,
        //проще всего было бы хранить полеты в таблице и выбирать их используя SELECT, собирая из разных сервисов кусочки строки WHERE
    }
}