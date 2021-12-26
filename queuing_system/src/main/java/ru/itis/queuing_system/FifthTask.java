package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;

import java.sql.Timestamp;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FifthTask extends AbstractTask {

    @Override
    public void avgAndDispersion(List<Timestamp> dates) {
        Map<MutablePair<Month, Integer>, Long> monthAndApplicationAmount = (dates.stream()
                .map(Timestamp::toLocalDateTime)
                .sorted()
                .collect(Collectors.groupingBy(date -> new MutablePair<>(date.getMonth(), date.getYear()), Collectors.counting())));

        Map<Month, Integer> monthAndAmountInList = new EnumMap<>(Month.class);
        for (Month m : Month.values()){
            monthAndAmountInList.putIfAbsent(m, 0);
        }

        monthAndApplicationAmount.forEach((key, value) -> {
            Month month = key.getKey();
            monthAndAmountInList.put(month, monthAndAmountInList.getOrDefault(month, 0) + 1);
            key.setValue(monthAndAmountInList.get(month));
        });

        monthAndApplicationAmount.forEach((key, value) -> log.info(
                "Месяц {} № {}, кол-во заявок {}",
                key.getKey().getDisplayName(TextStyle.FULL, Locale.ROOT),
                key.getValue(),
                value
        ));

        getDispersion(getAverage(monthAndApplicationAmount.values()), monthAndApplicationAmount.values());
    }

    @Override
    protected void getDispersion(double avg,
                                 Collection<Long> applicationAmountInMonth
    ) {
        var sum = applicationAmountInMonth.stream().map(
                        l -> Math.pow((l - avg), 2)
                )
                .reduce(0.0, Double::sum);
        var dispersion = Math.sqrt(sum / (applicationAmountInMonth.size()));

        log.info("Средне квадратичная ошибка = {} (кол. заявок/час)\n", dispersion / 30 / 24);
    }

    @Override
    protected double getAverage(Collection<Long> applicationAmountInMonth) {
        var avg = (double) applicationAmountInMonth.stream()
                .reduce(0L, Long::sum) / applicationAmountInMonth.size();
        log.info("Среднее значение = {} (кол. заявок/час)\n", avg / 7 / 24);
        return avg;
    }
}
