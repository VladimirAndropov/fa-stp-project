/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.accessingdatarest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author Josh Long
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AccessingDataRestApplicationTests {


	private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
			MediaType.APPLICATION_JSON.getSubtype(),
			Charset.forName("utf8"));

	private MockMvc mockMvc;

	private String userName = "bdussault";

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private Person person;

	private List<Bookmark> bookmarkList = new ArrayList<>();

	@Autowired
	private BookmarkRepository bookmarkRepository;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
				hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

	}

	@Test
	public void setup() throws Exception {
		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.bookmarkRepository.deleteAllInBatch();
		this.personRepository.deleteAllInBatch();

		this.person = personRepository.save(new Person(userName, "password"));
		this.bookmarkList.add(bookmarkRepository.save(new Bookmark(person, "http://bookmark.com/1/" + userName, "A description")));
		this.bookmarkList.add(bookmarkRepository.save(new Bookmark(person, "http://bookmark.com/2/" + userName, "A description")));
	}

	@Test
	public void userNotFound() throws Exception {
		mockMvc.perform(post("/george/bookmarks/")
						.content(this.json(new Bookmark()))
						.contentType(contentType))
				.andExpect(status().isNotFound());
	}

	@Test
	public void readSingleBookmark() throws Exception {
		mockMvc.perform(get("/" + userName + "/bookmarks/"
						+ this.bookmarkList.get(0).getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType))
				.andExpect(jsonPath("$.id", is(this.bookmarkList.get(0).getId().intValue())))
				.andExpect(jsonPath("$.uri", is("http://bookmark.com/1/" + userName)))
				.andExpect(jsonPath("$.description", is("A description")));
	}

	@Test
	public void readBookmarks() throws Exception {
		mockMvc.perform(get("/" + userName + "/bookmarks"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(contentType))
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].id", is(this.bookmarkList.get(0).getId().intValue())))
				.andExpect(jsonPath("$[0].uri", is("http://bookmark.com/1/" + userName)))
				.andExpect(jsonPath("$[0].description", is("A description")))
				.andExpect(jsonPath("$[1].id", is(this.bookmarkList.get(1).getId().intValue())))
				.andExpect(jsonPath("$[1].uri", is("http://bookmark.com/2/" + userName)))
				.andExpect(jsonPath("$[1].description", is("A description")));
	}

	@Test
	public void createBookmark() throws Exception {
		String bookmarkJson = json(new Bookmark(
				this.person, "http://spring.io", "a bookmark to the best resource for Spring news and information"));
		this.mockMvc.perform(post("/" + userName + "/bookmarks")
						.contentType(contentType)
						.content(bookmarkJson))
				.andExpect(status().isCreated());
	}

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(
				o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
