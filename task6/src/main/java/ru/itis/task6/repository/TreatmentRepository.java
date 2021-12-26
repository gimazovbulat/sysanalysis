package ru.itis.task6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.task6.model.Treatment;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
}
