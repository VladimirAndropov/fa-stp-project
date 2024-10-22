// tag::runner[]
package com.example.accessingdatarest;

        import java.util.Arrays;
        import java.util.Collection;

        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.boot.CommandLineRunner;
        import org.springframework.boot.SpringApplication;
        import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.ComponentScan;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.context.annotation.Import;
        import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
        import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
        import org.springframework.http.HttpHeaders;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.PathVariable;
        import org.springframework.web.bind.annotation.RequestBody;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestMethod;
        import org.springframework.web.bind.annotation.ResponseStatus;
        import org.springframework.web.bind.annotation.RestController;
        import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Configuration
@EnableJpaRepositories
@Import(RepositoryRestMvcConfiguration.class)
@ComponentScan
@EnableAutoConfiguration
public class Application {

    @Bean
    CommandLineRunner init(PersonRepository personRepository,
                           BookmarkRepository bookmarkRepository) {
        return (evt) -> Arrays.asList(
                        "jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
                .forEach(
                        a -> {
                            Person person = personRepository.save(new Person(a,
                                    "password"));
                            bookmarkRepository.save(new Bookmark(person,
                                    "http://bookmark.com/1/" + a, "A description"));
                            bookmarkRepository.save(new Bookmark(person,
                                    "http://bookmark.com/2/" + a, "A description"));
                        });
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
// end::runner[]

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

    private final BookmarkRepository bookmarkRepository;

    private final PersonRepository personRepository;

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {
        this.validateUser(userId);
        return this.personRepository
                .findByUsername(userId)
                .map(person -> {
                    Bookmark result = bookmarkRepository.save(new Bookmark(person,
                            input.uri, input.description));

                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setLocation(ServletUriComponentsBuilder
                            .fromCurrentRequest().path("/{id}")
                            .buildAndExpand(result.getId()).toUri());
                    return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
                }).get();

    }

    @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    Bookmark readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
        this.validateUser(userId);
        return this.bookmarkRepository.getOne(bookmarkId);
    }

    @RequestMapping(method = RequestMethod.GET)
    Collection<Bookmark> readBookmarks(@PathVariable String userId) {
        this.validateUser(userId);
        return this.bookmarkRepository.findByPersonUsername(userId);
    }

    @Autowired
    BookmarkRestController(BookmarkRepository bookmarkRepository,
                           PersonRepository personRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.personRepository = personRepository;
    }

    private void validateUser(String userId) {
        this.personRepository.findByUsername(userId).orElseThrow(
                () -> new UserNotFoundException(userId));
    }
}

@ResponseStatus(HttpStatus.NOT_FOUND)
class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("could not find user '" + userId + "'.");
    }
}