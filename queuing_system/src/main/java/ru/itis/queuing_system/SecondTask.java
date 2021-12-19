package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SecondTask extends AbstractTask {

    @Override
    public void avgAndDispersion(List<Timestamp> dates) {
        Map<Integer, Long> dayOfMonthAndAmountOfApplications = dates.stream().map(Timestamp::toLocalDateTime)
                .filter(t -> t.getYear() == 2020)
                .filter(t -> t.getMonth() == Month.OCTOBER)
                .collect(Collectors.groupingBy(LocalDateTime::getDayOfMonth, Collectors.counting()));
        dayOfMonthAndAmountOfApplications
                .forEach((key, value) -> log.info("День месяца = {}; Кол-во заявок = {}\n", key, value));

        getDispersion(getAverage(dayOfMonthAndAmountOfApplications), dayOfMonthAndAmountOfApplications);
    }

    @Override
    protected void getDispersion(double avg, Map<?, Long> applicationAmountInDays) {
        var sum = applicationAmountInDays.values().stream().map(
                        l -> Math.pow((l - avg), 2)
                )
                .reduce(0.0, Double::sum);
        var dispersion = Math.sqrt(sum / (applicationAmountInDays.size()));

        log.info("Средне квадратичная ошибка = {} (кол. заявок/час)\n", dispersion / 24);
    }

    @Override
    protected double getAverage(Map<?, Long> applicationAmountInWhatever) {
        var avg = (double) applicationAmountInWhatever.values().stream()
                .reduce(0L, Long::sum) / applicationAmountInWhatever.size();
        log.info("Среднее значение = {} (кол. заявок/час)\n", avg / 24);
        return avg;
    }
}
