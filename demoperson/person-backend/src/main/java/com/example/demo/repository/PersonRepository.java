package com.example.demo.repositories;

import com.example.demo.entities.Person;
import com.example.demo.entities.PersonRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

    /**
     * Example: JPA generated query by existing field
     */
    List<Person> findByName(String name);

    /**
     * Find person by username (for authentication)
     */
    Optional<Person> findByUsername(String username);

    /**
     * Find persons by role
     */
    List<Person> findByRole(PersonRole role);

    /**
     * Example: Custom query - find seniors by name
     */
    @Query(value = "SELECT p " +
            "FROM Person p " +
            "WHERE p.name = :name " +
            "AND p.age >= 60")
    Optional<Person> findSeniorsByName(@Param("name") String name);
}