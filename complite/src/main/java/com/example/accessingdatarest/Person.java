package com.example.accessingdatarest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;
@Entity
public class Person {
	@OneToMany(mappedBy = "person")
	private Set<Bookmark> bookmarks = new HashSet<>();
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	public Set<Bookmark> getBookmarks() {
		return bookmarks;
	}
	private String firstName;
	private String lastName;

	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}

	@JsonIgnore
	public String username;
	public String password;
	private String email;
	private String phone;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Person(String username, String password) {
		this.username = username;
		this.password = password;
	}

	Person() { // jpa only
	}
}
