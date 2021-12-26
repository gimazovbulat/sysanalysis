package ru.itis.task6.model;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;

@Data
@Entity
public class Conclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conclusion_seq_gen")
    @SequenceGenerator(
            name = "conclusion_seq_gen",
            sequenceName = "conclusion_seq",
            allocationSize = 1)
    private Long id;

    private String description;

    @Transient
    private Boolean isTerminal;

    @OneToOne
    @JoinColumn(name = "treatment", referencedColumnName = "id")
    private Treatment treatment;

    private String question;

    @OneToOne
    @JoinColumn(name = "negative", referencedColumnName = "id")
    private Conclusion negative;

    @OneToOne
    @JoinColumn(name = "positive", referencedColumnName = "id")
    private Conclusion positive;

    @PostLoad
    void fillIsTerminal(){
        this.isTerminal = Objects.nonNull(treatment);
    }
}
