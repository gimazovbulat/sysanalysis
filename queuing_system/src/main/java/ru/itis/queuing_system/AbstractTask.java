package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class AbstractTask {

    public abstract void avgAndDispersion(List<Timestamp> dates);

    protected abstract void getDispersion(
            double avg,
            Map<?, Long> applicationAmountInWhatever
    );

    protected abstract double getAverage(Map<?, Long> applicationAmountInWhatever);

}
