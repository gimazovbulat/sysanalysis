package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.sql.Timestamp;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
public class FifthTask extends AbstractTask {

    @Override
    public void avgAndDispersion(List<Timestamp> dates) {
        AtomicReference<Map<ImmutablePair<Month, Integer>, Long>> monthAndApplicationAmount = new AtomicReference<>();
        Arrays.asList(Month.values()).forEach(month -> {
            monthAndApplicationAmount.set(dates.stream()
                    .map(Timestamp::toLocalDateTime)
                    .filter(t -> t.getMonth() == month)
                    .collect(Collectors.groupingBy(date -> new ImmutablePair<>(date.getMonth(), date.getYear()), Collectors.counting())));
        });

        Integer earliestYear = monthAndApplicationAmount.get().keySet().stream()
                .min(Comparator.comparingInt(ImmutablePair::getValue))
                .map(ImmutablePair::getValue)
                .orElseThrow(() -> new IllegalStateException("something went wrong"));

        monthAndApplicationAmount.get().forEach((key, value) -> key.setValue(key.getValue() - earliestYear + 1));

        monthAndApplicationAmount.get().forEach((key, value) -> log.info(
                "Месяц {} № {}, кол-во заявок {}",
                key.getKey().getDisplayName(TextStyle.FULL, Locale.ROOT),
                key.getValue(),
                value
        ));

        getDispersion(getAverage(monthAndApplicationAmount.get()), monthAndApplicationAmount.get());
    }

    @Override
    protected void getDispersion(double avg,
                                 Map<?, Long> applicationAmountInMonth
    ) {
        var sum = applicationAmountInMonth.values().stream().map(
                        l -> Math.pow((l - avg), 2)
                )
                .reduce(0.0, Double::sum);
        var dispersion = Math.sqrt(sum / (applicationAmountInMonth.size()));

        log.info("Средне квадратичная ошибка = {} (кол. заявок/час)\n", dispersion / 30 / 24);
    }

    @Override
    protected double getAverage(Map<?, Long> applicationAmountInMonth) {
        var avg = (double) applicationAmountInMonth.values().stream()
                .reduce(0L, Long::sum) / applicationAmountInMonth.size();
        log.info("Среднее значение = {} (кол. заявок/час)\n", avg / 7 / 24);
        return avg;
    }
}
