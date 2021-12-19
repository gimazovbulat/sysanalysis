package ru.itis.queuing_system;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.FileReader;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CsvReader {

    public List<Timestamp> readCsv() {
        ClassPathResource resource = new ClassPathResource("dataset.csv");

        try (CSVReader reader = new CSVReader(new FileReader(resource.getFile()))) {
            return reader.readAll().stream()
                    .map(arr -> Timestamp.valueOf(Arrays.stream(arr).reduce(String::concat).get()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalStateException(e);
        }
    }
}
