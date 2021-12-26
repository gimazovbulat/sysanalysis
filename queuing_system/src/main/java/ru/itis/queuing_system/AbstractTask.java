package ru.itis.queuing_system;

import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Slf4j
public abstract class AbstractTask {

    public abstract void avgAndDispersion(List<Timestamp> dates);

    protected abstract void getDispersion(
            double avg,
            Collection<Long> applicationAmountInWhatever
    );

    protected abstract double getAverage(Collection<Long> applicationAmountInWhatever);

}
