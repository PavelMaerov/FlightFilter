package com.gridnine.testing;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FlightUtils {

    /**
     * Этот статический метод заменяет длинную конструкцию для фильтрации списка полетов с помощью предиката
     * flightList.stream().filter(predicate).collect(Collectors.toList())
     * на немного более короткую
     * filter(flightList, predicate)

     * @param flights исходная коллекция полетов (список, множество) для фильтрации
     * @param predicate фильтр для полета - ссылка на объект, реализующий интерфейс Predicate
     *                  (т.е. с методом test, возвращающий true|false) для полета
     * @return List<ExtendedFlight> - список полетов
     */
    public static List<ExtendedFlight> filter(Collection<ExtendedFlight> flights, Predicate<ExtendedFlight> predicate) {
        //При больших объемах до .filter можно добавить .parallel().
        return flights.stream().filter(predicate).collect(Collectors.toList());
    }

    /**
     * Этот метод фильтрует список полетов, используя список предикатов, соединяя их по короткой схеме "И"
     * Реально все действия сводятся к созданию нового предиката, вызывающего последовательно
     * методы test всех предикатов из списка, до первого, возвращающего false.
     * Если никто false не вернул, возвращается true
     * Созданный предикат применяется к списку полетов методом filter
     * Название метода "filter" общее и для одиночного условия, и для группового.
     *
     * @param flights исходная коллекция полетов (список, множество) для фильтрации
     * @param predicates список предикатов для полета
     * @return List<ExtendedFlight> - список полетов
     */
    public static List<ExtendedFlight> filter(Collection<ExtendedFlight>  flights,
                                              List<Predicate<ExtendedFlight>> predicates) {
        return filter(flights, flight ->{  //пишем лямбду-predicate для списка условий
            for (Predicate<ExtendedFlight> predicate:predicates) {
                if (!(predicate.test(flight))) return false;
            }
            return true;
        });
    }

    //Казалось бы одного предыдущего метода достаточно
    //для предоставления всех удобств фильтрации перебором
    //и можно считать задание выполненным.
    //Однако есть еще предложение.

    //Для быстрого отбора полетов их надо упорядочить
    //Сравнивать полеты сложно - это же целая последовательность сегментов, которую надо перебирать.
    //Поэтому можно собрать возможные интегральные характеристики полета в классе наследнике Flight
    //и создавать разные TreeSet из них, используя компараторы по разным полям.
    //Тогда можно выбирать без задержек интервалы этих TreeSet в заданных границах.
    //Однако структура для хранения вырастет на величину всевозможных характеристик полетов,
    //и добавление новой характеристики потребует переделки этого модуля

    //Я бы хотел предложить использовать структуру TreeMap,
    //где характеристика полета будет сохранена в ключе, а значение - полет.
    //Характеристику полета надо преобразовать в осмысленное целое число (индекс),
    //по которому полеты можно упорядочить.

    //Возможные варианты индекса:
    //Время вылета - число секунд с 1970
    //Количество бракованных сегментов, где дата вылета больше, чем прилета
    //Общее время пересадок в минутах

    //Однако, чтобы это число использовать в качестве ключа в TreeMap полетов - оно еще должно быть уникальным.
    //Учитывая, что количество полетов ограничено 1e9, предложу в качестве ключа использовать
    //выражение типа long = Индекс * 1e9 + счетчикПолета
    //Такой ключ и сохраняет сортировку по индексу, и является уникальным.

    /**
     * Создает дерево с ключами, вычисленными с помощью keyFunction от полетов,
     * и значениями - полетами.
     * @param collection исходная коллекция полетов
     * @param keyFunction функция создания ключа дерева
     * @return TreeMap<Long, ExtendedFlight> - дерево полетов
     */
    public static TreeMap<Long, ExtendedFlight> createTreeMap(
            Collection<ExtendedFlight> collection,
            Function<ExtendedFlight, Long> keyFunction) {

        TreeMap<Long, ExtendedFlight> map = new TreeMap<>();
        int count = 1;
        //Из лямбды счетчик менять нельзя (ведь ссылка на нее может быть кому-нибудь передана и там count не будет)
        //list.forEach(flight -> map.put(indexFunction.apply(flight) + count++, flight));
        for (ExtendedFlight flight : collection) {
            map.put(keyFunction.apply(flight) + count++, flight);
        }
        return map;
    }

    //Ключи для построения деревьев. Сейчас написаны для date-полей и int-полей

    /**
     * Это статический метод, превращающий дату в ключ дерева. К полету не привязан.
     * Метод просто вычисляет число секунд с 1970 и сдвигает его на 9 знаков,
     * чтобы при записи полета в дерево смочь добавить еще уникальный счетчик полета.
     * Используется и для упаковки в дерево, и для задания границ отбора в дереве
     * @return long
     */
    public static long dateToKey(LocalDateTime dateTime){
        return dateTime.toEpochSecond(ZoneOffset.UTC) * 1000000000L;
    }

    /**
     * Ключ дерева для Integer - индексов
     * Если индекс - целое число, то это почти готовый ключ для карты, осталось сдвинуть его в верхнюю часть,
     * чтобы в нижнюю уместился счетчик
     * @return long
     */
    public static long IntegerToKey(int i){
        return i * 1000000000L;
    }
}
