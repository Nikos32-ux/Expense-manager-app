package com.example.ExpenseTracker.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status = "IDLE";

    @Column(nullable = false )
    private boolean isChanged = false;

    @Column(nullable = false )
    private boolean wsSent = false;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String csvFile;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
