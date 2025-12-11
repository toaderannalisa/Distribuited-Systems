package com.example.demo.dtos;

import com.example.demo.entities.PersonRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.demo.dtos.validators.annotation.AgeLimit;

import java.util.Objects;
import java.util.UUID;

public class PersonDetailsDTO {
    private UUID id;

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotBlank(message = "address is required")
    private String address;

    @NotNull(message = "age is required")
    @AgeLimit(value = 18)
    private Integer age;

    @NotNull(message = "role is required")
    private PersonRole role;

    public PersonDetailsDTO() {
    }

    public PersonDetailsDTO(String name, String username, String password, String address, int age, PersonRole role) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.address = address;
        this.age = age;
        this.role = role;
    }

    public PersonDetailsDTO(UUID id, String name, String username, String address, int age, PersonRole role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.address = address;
        this.age = age;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public PersonRole getRole() { return role; }
    public void setRole(PersonRole role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonDetailsDTO that = (PersonDetailsDTO) o;
        return age == that.age &&
                Objects.equals(name, that.name) &&
                Objects.equals(username, that.username) &&
                Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, username, address, age);
    }
}