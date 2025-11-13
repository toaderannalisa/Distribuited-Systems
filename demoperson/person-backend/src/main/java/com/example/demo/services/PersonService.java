package com.example.demo.services;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.dtos.builders.PersonBuilder;
import com.example.demo.entities.Person;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonService(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<PersonDTO> findPersons() {
        List<Person> personList = personRepository.findAll();
        return personList.stream()
                .map(PersonBuilder::toPersonDTO)
                .collect(Collectors.toList());
    }

    public PersonDetailsDTO findPersonById(UUID id) {
        Optional<Person> personOptional = personRepository.findById(id);
        if (!personOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        return PersonBuilder.toPersonDetailsDTO(personOptional.get());
    }

    public PersonDetailsDTO findPersonByUsername(String username) {
        Optional<Person> personOptional = personRepository.findByUsername(username);
        if (!personOptional.isPresent()) {
            LOGGER.error("Person with username {} was not found in db", username);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with username: " + username);
        }
        return PersonBuilder.toPersonDetailsDTO(personOptional.get());
    }

    @Transactional
    public UUID insert(PersonDetailsDTO personDTO) {
        // Encode password before saving
        Person person = PersonBuilder.toEntity(personDTO);
        person.setPassword(passwordEncoder.encode(person.getPassword()));

        person = personRepository.save(person);
        LOGGER.debug("Person with id {} was inserted in db", person.getId());
        return person.getId();
    }

    @Transactional
    public UUID updateById(UUID id, PersonDetailsDTO personDTO) {
        Optional<Person> existingPersonOpt = personRepository.findById(id);
        if (!existingPersonOpt.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }

        Person person = existingPersonOpt.get();
        person.setName(personDTO.getName());
        person.setUsername(personDTO.getUsername());

        // Only update password if it's provided and not empty
        if (personDTO.getPassword() != null && !personDTO.getPassword().isEmpty()) {
            person.setPassword(passwordEncoder.encode(personDTO.getPassword()));
        }

        person.setAddress(personDTO.getAddress());
        person.setAge(personDTO.getAge());
        person.setRole(personDTO.getRole());
        person = personRepository.save(person);

        LOGGER.debug("Person with id {} was updated in db", person.getId());
        return person.getId();
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!personRepository.existsById(id)) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        personRepository.deleteById(id);
        LOGGER.debug("Person with id {} was deleted from db", id);
    }
    public java.util.Optional<org.springframework.security.core.userdetails.UserDetails> loadUserByUsername(String username) {
        com.example.demo.entities.Person person = personRepository.findByUsername(username)
                .orElse(null);

        if (person == null) {
            return java.util.Optional.empty();
        }

        // Creăm un obiect UserDetails pe care Spring Security îl înțelege
        return java.util.Optional.of(
                org.springframework.security.core.userdetails.User.builder()
                        .username(person.getUsername())
                        .password(person.getPassword()) // Asigură-te că parola este Criptată în DB
                        .roles(person.getRole().name())
                        .build()
        );
    }
}