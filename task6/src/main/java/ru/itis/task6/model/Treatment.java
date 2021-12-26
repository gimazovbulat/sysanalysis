package ru.itis.task6.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "treatment_seq_gen")
    @SequenceGenerator(
            name = "treatment_seq_gen",
            sequenceName = "treatment_seq",
            allocationSize = 1)
    private Long id;

    private String description;
}
