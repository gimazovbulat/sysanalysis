package ru.itis.task6;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.itis.task6.model.Conclusion;
import ru.itis.task6.repository.ConclusionRepository;
import ru.itis.task6.repository.TreatmentRepository;

import javax.persistence.EntityNotFoundException;
import java.util.Objects;
import java.util.Scanner;

@SpringBootApplication
public class Task6Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Task6Application.class, args);
    }

    @Autowired
    private ConclusionRepository conclusionRepository;

    @Override
    public void run(String... args) throws Exception {
        Conclusion startPoint = conclusionRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("no entity found"));

        Scanner scanner = new Scanner(System.in);

        Conclusion conclusion = startPoint;
        while (Objects.nonNull(conclusion) && !conclusion.getIsTerminal()) {
            System.out.println(conclusion.getQuestion());
            System.out.println("Please write 'y' or 'n'");
            String answer = scanner.next();

            while (!answer.equals("y") && !answer.equals("n")){
                System.out.println("Your input was: " + answer);
                System.out.println("Please write 'y' or 'n'");
                answer = scanner.next();
            }

            conclusion = answer.equals("y") ? conclusion.getPositive() : conclusion.getNegative();
        }

        System.out.println(conclusion.getTreatment().getDescription());
    }
}
