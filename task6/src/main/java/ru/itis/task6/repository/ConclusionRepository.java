package ru.itis.task6.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.task6.model.Conclusion;

public interface ConclusionRepository extends JpaRepository<Conclusion, Long> {
}
