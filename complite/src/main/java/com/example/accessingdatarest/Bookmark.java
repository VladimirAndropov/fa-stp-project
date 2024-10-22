package com.example.accessingdatarest;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Bookmark {

    @JsonIgnore
    @ManyToOne
    private Person person;

    @Id
    @GeneratedValue
    private Long id;

    Bookmark() { // jpa only
    }

    public Bookmark(Person person, String uri, String description) {
        this.uri = uri;
        this.description = description;
        this.person = person;
    }

    public String uri;
    public String description;

    public Person getPerson() {
        return person;
    }

    public Long getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public String getDescription() {
        return description;
    }
}