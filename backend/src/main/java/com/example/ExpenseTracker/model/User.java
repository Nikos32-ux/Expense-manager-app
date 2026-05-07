package com.example.ExpenseTracker.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@Table(name="users")
public class User extends UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String imageProfile;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<Expense> expenses = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<Income> income = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    public Set<Notification> notifications = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name ="user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Roles> roles = new HashSet<>();

    @OneToOne(mappedBy = "user",fetch = FetchType.LAZY)
    Report report;
}
