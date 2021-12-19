package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class FourthTask extends AbstractTask{

    @Override
    public void avgAndDispersion(List<Timestamp> dates) {
        Map<LocalDate, Long> allApplications = new HashMap<>();
        var octoberApplications = dates.stream().map(Timestamp::toLocalDateTime)
                .filter(t -> t.getMonth() == Month.OCTOBER)
                .filter(t -> t.getDayOfWeek() == DayOfWeek.SUNDAY)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));
        var novemberApplications = dates.stream().map(Timestamp::toLocalDateTime)
                .filter(t -> t.getMonth() == Month.NOVEMBER)
                .filter(t -> t.getDayOfWeek() == DayOfWeek.SUNDAY)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));
        var decemberApplications = dates.stream().map(Timestamp::toLocalDateTime)
                .filter(t -> t.getMonth() == Month.DECEMBER)
                .filter(t -> t.getDayOfWeek() == DayOfWeek.SUNDAY)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));

        allApplications.putAll(octoberApplications);
        allApplications.putAll(novemberApplications);
        allApplications.putAll(decemberApplications);


        int i = 1;
        for (var monday : allApplications.values()) {
            log.info("Воскресенье №{}; Кол-во заявок = {}\n", i, monday);
            i++;
        }

        getDispersion(getAverage(allApplications), allApplications);
    }

    @Override
    protected void getDispersion(double avg, Map<?, Long> applicationAmountInWhatever) {
        var sum = applicationAmountInWhatever.values().stream().map(
                        l -> Math.pow((l - avg), 2)
                )
                .reduce(0.0, Double::sum);
        var dispersion = Math.sqrt(sum / (applicationAmountInWhatever.size()));

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
