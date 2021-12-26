package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Slf4j
public class FourthTask extends AbstractTask {

    @Override
    public void avgAndDispersion(List<Timestamp> dates) {
        List<Long> forOctober = countApplicationsForMonth(dates, Month.OCTOBER);
        List<Long> forSeptember = countApplicationsForMonth(dates, Month.SEPTEMBER);
        List<Long> forDecember = countApplicationsForMonth(dates, Month.DECEMBER);

        IntStream.range(0, forOctober.size()).forEach(
                i -> log.info("week # {} of october {}", i, forOctober.get(i))
        );

        IntStream.range(0, forSeptember.size()).forEach(
                i -> log.info("week # {} of september {}", i, forSeptember.get(i))
        );

        IntStream.range(0, forDecember.size()).forEach(
                i -> log.info("week # {} of december {}", i, forDecember.get(i))
        );

        log.info("for october");
        getDispersion(getAverage(forOctober), forOctober);
        log.info("/for october");

        log.info("for september");
        getDispersion(getAverage(forSeptember), forSeptember);
        log.info("/for september");

        log.info("for december");
        getDispersion(getAverage(forDecember), forDecember);
        log.info("/for december");
    }

    private List<Long> countApplicationsForMonth(List<Timestamp> dates, Month month) {
        List<Long> amountOfApplicationForMonth = new ArrayList<>();

        LocalDateTime lastDayOfMonth = dates.stream().map(Timestamp::toLocalDateTime)
                .filter(t -> t.getMonth() == month)
                .map(t -> t.withDayOfMonth(t.getMonth().length(t.toLocalDate().isLeapYear())))
                .findFirst().get();

        LocalDateTime firstDayOfMonth = dates.stream().map(Timestamp::toLocalDateTime)
                .filter(t -> t.getMonth() == month && t.getDayOfMonth() == 1)
                .findFirst().get();

        LocalDateTime firstSundayOfMonth = dates.stream().map(Timestamp::toLocalDateTime)
                .filter(t -> t.getMonth() == month && t.getDayOfWeek() == DayOfWeek.SUNDAY)
                .findFirst().get();

        final AtomicReference<LocalDateTime> end = new AtomicReference<>(firstSundayOfMonth);
        final AtomicReference<LocalDateTime> start = new AtomicReference<>(firstDayOfMonth);

        while (!start.get().isAfter(lastDayOfMonth)) {
            long amountOfApplicationForWeek = dates.stream().map(Timestamp::toLocalDateTime)
                    .filter(t -> t.getYear() == 2020 && t.getMonth() == month)
                    .filter(t ->
                            t.isAfter(start.get().minus(1, ChronoUnit.DAYS)) &&
                                    t.isBefore(end.get().plus(1, ChronoUnit.DAYS))
                    ).count();

            start.set(end.get().plus(1, ChronoUnit.DAYS));
            end.set(end.get().plus(7, ChronoUnit.DAYS));
            end.set(end.get().isAfter(lastDayOfMonth) ? lastDayOfMonth : end.get());

            amountOfApplicationForMonth.add(amountOfApplicationForWeek);
        }

        return amountOfApplicationForMonth;
    }

        @Override
        protected void getDispersion (double avg, Collection<Long > applicationAmountInWhatever){
            var sum = applicationAmountInWhatever.stream().map(
                            l -> Math.pow((l - avg), 2)
                    )
                    .reduce(0.0, Double::sum);
            var dispersion = Math.sqrt(sum / (applicationAmountInWhatever.size()));

            log.info("Средне квадратичная ошибка = {} (кол. заявок/час)\n", dispersion / 7 / 24);

        }

        @Override
        protected double getAverage (Collection < Long > applicationAmountInWhatever) {
            var avg = (double) applicationAmountInWhatever.stream()
                    .reduce(0L, Long::sum) / applicationAmountInWhatever.size();
            log.info("Среднее значение = {} (кол. заявок/час)\n", avg / 7 / 24);
            return avg;
        }
    }
