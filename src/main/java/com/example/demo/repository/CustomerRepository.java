package com.example.demo.repository;

import com.example.demo.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("""
        SELECT c
        FROM Customer c
        LEFT JOIN FETCH c.transactions
        WHERE c.id = :id
    """)
    Optional<Customer> findByIdWithTransactions(Long id);
}