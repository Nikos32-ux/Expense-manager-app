package com.example.ExpenseTracker.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "capabilities")
public class Capabilities {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String capabilityType;
    @ManyToMany(mappedBy = "capabilities", fetch = FetchType.LAZY)
    List<Roles> roles = new ArrayList<>();
}
