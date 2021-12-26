package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SixthTask extends AbstractTask {

    @Override
    public void avgAndDispersion(List<Timestamp> dates) {
        Map<Month, Long> applications = dates.stream().map(Timestamp::toLocalDateTime)
                .collect(Collectors.groupingBy(LocalDateTime::getMonth, Collectors.counting()));
        applications
                .forEach((key, value) -> log.info("Месяц = {}; Кол-во заявок = {}\n",
                        key.getDisplayName(
                        TextStyle.FULL, Locale.ENGLISH), value)
                );

        getDispersion(getAverage(applications.values()), applications.values());
    }

    @Override
    protected void getDispersion(double avg, Collection<Long> applicationAmountInWhatever) {
        var sum = applicationAmountInWhatever.stream().map(
                        l -> Math.pow((l - avg), 2)
                )
                .reduce(0.0, Double::sum);

        var dispersion = Math.sqrt(sum / (applicationAmountInWhatever.size()));

        log.info("Средне квадратичная ошибка = {} (кол. заявок/час)\n", dispersion / 30.5 / 24);
    }

    @Override
    protected double getAverage(Collection<Long> applicationAmountInWhatever) {
        var avg = (double) applicationAmountInWhatever.stream()
                .reduce(0L, Long::sum) / applicationAmountInWhatever.size();
        log.info("Среднее значение = {} (кол. заявок/час)\n", avg /30.5 / 24);
        return avg;
    }
}
