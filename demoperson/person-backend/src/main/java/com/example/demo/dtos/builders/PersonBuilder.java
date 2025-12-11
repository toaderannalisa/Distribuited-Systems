package com.example.demo.dtos.builders;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.entities.Person;
import com.example.demo.entities.PersonRole;

public class PersonBuilder {

    private PersonBuilder() {
    }

    public static PersonDTO toPersonDTO(Person person) {
        return new PersonDTO(
                person.getId(),
                person.getName(),
                person.getUsername(),
                person.getAge(),
                person.getRole(),
                person.getAddress()
        );
    }

    public static PersonDetailsDTO toPersonDetailsDTO(Person person) {
        PersonDetailsDTO dto = new PersonDetailsDTO(
                person.getId(),
                person.getName(),
                person.getUsername(),
                person.getAddress(),
                person.getAge(),
                person.getRole()
        );
        dto.setPassword(person.getPassword());
        return dto;
    }

    public static Person toEntity(PersonDetailsDTO personDetailsDTO) {
        return new Person(
                personDetailsDTO.getName(),
                personDetailsDTO.getUsername(),
                personDetailsDTO.getPassword(),
                personDetailsDTO.getAddress(),
                personDetailsDTO.getAge(),
                personDetailsDTO.getRole() != null ? personDetailsDTO.getRole() : PersonRole.CLIENT
        );
    }
}