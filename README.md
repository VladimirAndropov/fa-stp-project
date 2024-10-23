

# Учебные материалы по написанию курсовой работы по дисциплине "Современные технологии программирования"

Создание REST клиент-серверного приложения с помощью Spring
=========================================

REST быстро стал де-факто стандартом для создания web-сервисов, поскольку таким они легки для создания и обработки.

Существует большое обсуждение о том, как REST вписывается в мир микросервисов, но в этом уроке просто описывается создание RESTful сервисов.

Почему REST? Из [REST на практике](//www.amazon.com/gp/product/0596805829?ie=UTF8&tag=martinfowlerc-20&linkCode=as2&camp=1789&creative=9325&creativeASIN=0596805829), заимствуется фраза [Martin Flower](//martinfowler.com/): "Понятие того, что web является доказательством очень масштабируемой распределенной системы, которая работает действительно хорошо и мы можем брать идеи, исходя из того, чтобы создавать системы более легко." Я думаю, что это довольно веская причина: REST охватывает указания самого web, его архитектуры и его приемуществ.

Какие преимущества? Главным образом все те, какие несет в себе HTTP как платформа сама по себе. Безопасность приложений(шифрование и аутентификация), для которой известно некоторое количество решений. Кеширование встроено в протокол. Маршрутизация, через DNS, гибкая и хорошо известная система уже поддерживается повсеместно.

REST, несмотря на повсеместность использования, не является стандартом, как таковой, а подходом, стилем, ограниечением HTTP протокола. Его реализация может различаться в стиле, подходе. Для клиента такого API это может быть разочарованием. Качество REST сервисов дико варьируется.

Dr. Leonard Richardson собрал воедино модель, которая объясняет различные уровни соответствующих понятий REST и сортирует их. Она описывает 4 уровня, начиная с **level 0**. Martin Fowler [очень хорошо написал про модель](//martinfowler.com/articles/richardsonMaturityModel.html)

*   **Level 0**: Swamp of POX - это уровень, где мы просто используем HTTP как транспорт. Вы можете вызвать SOAP технологию. Она использует HTTP, но как транспорт. Стоит также отметить, что вы можете также использовать SOAP [поверх чего-то подобного JMS](//www.w3.org/TR/soapjms/) без HTTP. SOAP, соответветственно не является RESTful. Он всего лишь HTTP-aware.
*   **Level 1**: Resources - на этом уровне сервисы могут использовать HTTP URI для отличия между сущностями в системе. К примеру, вы можете направить запросы на `/customers`, `/users` и т.д. XML-RPC является примером **Level 1** технологии: он использует HTTP и он может использовать URI для различия точек выхода. В конечном счете, несмотря на то, что XML-RPC не является RESTful, он использует HTTP как транспорт для чего-нибудь ещё(удаленный вызов процедур).
*   **Level 2**: HTTP Verbs - это уровень, на котором вы хотите быть. Если вы делаете все неправильно с Spring MVC, то вы скорее всего все ещё остановитесь тут. На этом уровне сервисы используют преимущества нативных HTTP возможностей, таким как заголовки, коды статуса, определенные URI и другие. Отсюда мы начнем наше путешествие.
*   **Level 3**: Hypermedia Controls - это заключительный уровень, к которому мы стремимся. Гипермедиа как практическое применение [HATEOAS](//en.wikipedia.org/wiki/HATEOAS) ("HATEOAS" является сокращением от "Hypermedia as the Engine of Application State") шаблона проектирования. Гипермедиа продлевает жизнь сервису, отделяя клиента сервиса от необходимости глубокого знания платформы и топологии сервиса. Она описывает REST сервисы. Сервис может ответить на вопросо о том, какой выл вызов и когда. Мы рассмотрим более глубже в дальнейшем.


 ![](1.png)

Начало работы
-------------

Для работы в этом уроке мы будем использовать [Spring Boot](https://spring-projects.ru/projects/spring-boot/), который позволяет избежать множество шаблонной разработки. Вы можете начать с [Spring Initializr](//start.spring.io/) и выбрать флажками соответствующий тип приложения, который будет поддерживаться. В данном случае, мы выбрали создание **web** приложения. 

 ![](2024-10-23_10-35-16.png)

Итак, выберите "Web", а затем нажмите "Generate". Начнется загрузка zip-файла. Распакуйте его. В нем вы найдете простую Maven или Gradle структуру каталогов, содержащим Maven `pom.xml` и Gradle `build.gradle`. Большинство людей используют Maven, поэтому в этом уроке будет использован Maven. Кстати, если вы еще не знакомы с Gradle, познакомьтесь. Он очень хорош.

Spring Boot может работать с любой IDE. Вы можете использовать Eclipse, IntelliJ IDEA, Netbeans и другие. [Spring Tool Suite](//spring.io/tools/) является IDE с открытым исходным кодом, основанной на Eclipse, которая предоставляет расширенные возможности Java EE. Она включает возможности для ещё более простого создания Spring приложений. Но присмотритесь к нему, если вы хотите обрести дополнительное преимущество к вашим ключевым действиям. Ниже представлено видео, которое демонстрирует как начать работу с STS и Spring Boot. Это общая инструкция для ознакомления с инструментами.

Код приложения
----------------------


Мы не будем погружаться в слишком большое количество кода. Мы используем две JPA сущности для моделирования записи в БД. Мы используем стандартную SQL БД для сохранения наших записей, чтобы имелась возможность использовать их как можно большему числу клиентов.

Первый класс моделирует учетную запись пользователя.

Поразительно, но ниже приведенный класс является одним из самых _шумных_ - в основном из-за многословности языка Java.

`model/src/main/java/bookmarks/Account.java`

    package bookmarks;
    
    import com.fasterxml.jackson.annotation.JsonIgnore;
    
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;
    import javax.persistence.OneToMany;
    import java.util.HashSet;
    import java.util.Set;
    
    
    @Entity
    public class Account {
    
        @OneToMany(mappedBy = "account")
        private Set<Bookmark> bookmarks = new HashSet<>();
    
        @Id
        @GeneratedValue
        private Long id;
    
        public Set<Bookmark> getBookmarks() {
            return bookmarks;
        }
    
        public Long getId() {
            return id;
        }
    
        public String getPassword() {
            return password;
        }
    
        public String getUsername() {
            return username;
        }
    
        @JsonIgnore
        public String password;
        public String username;
    
        public Account(String name, String password) {
            this.username = name;
            this.password = password;
        }
    
        Account() { // jpa only
        }
    }

Каждый `Account` может не иметь, либо иметь один или множество `Bookmark` сущностей. Это связь 1:N. Код сущности `Bookmark` показан ниже:

`model/src/main/java/bookmarks/Bookmark.java`

    package bookmarks;
    
    import com.fasterxml.jackson.annotation.JsonIgnore;
    
    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;
    import javax.persistence.ManyToOne;
    
    @Entity
    public class Bookmark {
    
        @JsonIgnore
        @ManyToOne
        private Account account;
    
        @Id
        @GeneratedValue
        private Long id;
    
        Bookmark() { // jpa only
        }
    
        public Bookmark(Account account, String uri, String description) {
            this.uri = uri;
            this.description = description;
            this.account = account;
        }
    
        public String uri;
        public String description;
    
        public Account getAccount() {
            return account;
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

Мы будем использовать [два Spring Data JPA репозитория](https://spring-projects.ru/guides/accessing-data-jpa) для обработки рутинных взаимодействий с БД. Spring Data репозитории как правило являются интерфейсами с методами, поддерживающими чтение, обновление, удаление и создание записей. Некоторые репозитории также поддерживают разделение на страницы и сортировку. Spring Data реализации основанных на соглашении именования методов в интерфейсе. Существует множество реализаций репозиториев, кроме тех двух. Вы можете использовать Spring Data MongoDB, Spring Data GemFire, Spring Data Cassandra и другие.

Один репозиторий будет управлять нашими `Account` сущностями, названный как `AccountRepository`. Один собственный поисковый метод `findByUsername` будет, в основном, создавать JPA запрос `select a from Account a where a.username = :username`, запускать его(передавая аргумент `username` метода как именованный параметр запроса) и возвращать результат.

`model/src/main/java/bookmarks/AccountRepository.java`

    package bookmarks;
    
    
    import org.springframework.data.jpa.repository.JpaRepository;
    
    import java.util.Optional;
    
    public interface AccountRepository extends JpaRepository<Account, Long> {
        Optional<Account> findByUsername(String username);
    }

Ниже представлен репозиторий для работы с `Bookmark` сущностью:

`model/src/main/java/bookmarks/BookmarkRepository.java`

    package bookmarks;
    
    
    import org.springframework.data.jpa.repository.JpaRepository;
    
    import java.util.Collection;
    
    public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
        Collection<Bookmark> findByAccountUsername(String username);
    }

`BookmarkRepository` имеет похожий поисковый метод, но разыменовывает поле `username` связи сущности `Bookmark` c `Account`, что в итоге требует присоединения. JPA запрос генерируется примерно таким: `SELECT b from Bookmark b WHERE b.account.username = :username`.

Наше приложение будет использовать Spring Boot. Spring Boot приложение, это как минимум, точка входа `public static void main` и аннотация `@EnableAutoConfiguration`. Это говорит о поддержке Spring Boot там, где это возможно. Наш `Application` класс также является хорошим местом для помещения разных вещей, например, `@Bean` определений. Вот как выглядит наш простейший `Application.java` класс:

    package bookmarks;
    
    import java.util.Arrays;
    import java.util.Collection;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.ComponentScan;
    import org.springframework.context.annotation.Configuration;
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
    @ComponentScan
    @EnableAutoConfiguration
    public class Application {
    
    	@Bean
    	CommandLineRunner init(AccountRepository accountRepository,
    			BookmarkRepository bookmarkRepository) {
    		return (evt) -> Arrays.asList(
    				"jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
    				.forEach(
    						a -> {
    							Account account = accountRepository.save(new Account(a,
    									"password"));
    							bookmarkRepository.save(new Bookmark(account,
    									"http://bookmark.com/1/" + a, "A description"));
    							bookmarkRepository.save(new Bookmark(account,
    									"http://bookmark.com/2/" + a, "A description"));
    						});
    	}
    
    	public static void main(String[] args) {
    		SpringApplication.run(Application.class, args);
    	}
    }

Как только приложение запустится, Spring Boot вызовет все бины типа `CommandLineRunner`, передавая им обратный вызов. В данном случае, `CommandLineRunner` является интерфейсом с одним абстрактным методом, который означает, что - в мире Java 8 - мы можем заменить его определение лямбда выражением. Все примеры в этом уроке будут использовать Java 8. Однако нет никаких оснований не использовать Java 6 или 7, просто замените более короткий синтаксис лямбда более подробным анонимным внутренним классом, реализующим интерфейс.

HTTP - это платформа
--------------------

HTTP URI является естесственным способом описания иерархии или взаимосвязей. К примеру, мы можем начать наш REST API на уровне учетной записи. Все URI начинаются с имени пользователя. Таким образом, для аккаунта `bob` мы можем обратиться как `/users/bob` или просто `/bob`. Для доступа к списку закладок пользователя мы можем опуститься на уровень ниже(подобно файловой системе) к ресурсу `/bob/bookmarks`.

REST не предписывает какое-то конкретное представление или кодировку. REST, сокращенно от **Representational STate Transfer** полагается на HTTP механизм согласования между клиентом и сервисом содержимого для взаимного понимания представления, полученного от сервиса, если это возможно. Существует множество способов для обработки согласования содержимого, но в самом простом случае, клиент отправляет запрос с заголовком `Accept`, в котором указывается через запятую список mime типов (например: `Accept: application/json, application/xml, */*`). Если сервис может обработать какой-либо из этих типов, то он ответит представлением первым из обрабатываемых типов.

Мы можем использовать HTTP глаголы для манипуляции данными, представленными по этим URI.

*   `GET` говорит сервису получить или вернуть ресурс, назначенный по URI. Конечно, делает он это через определенную реализацию. Код сервиса может обращаться к БД, файловой системе, другому web-сервису и др. Клиенту не обязательно знать об этом. Для клиента, все ресурсы являются HTTP ресурсами, и в мире HTTP есть только один способ спросить данные: `GET`. `GET` не имеет тела запроса, но обычно возвращает данные. Ответом на HTTP `GET` запрос `/bob/bookmarks/6` может быть примерно таким:
    
        {
            id: 6,
            uri: "http://bookmark.com/2/bob",
            description: "A description"
        }
    
*   `DELETE` говорит сервису удалить ресурс по назначенному URI. И снова, через определенную реализацию. `DELETE` не имеет тела.
*   `PUT` говорит сервису обновить ресурс по назначенному URI данными в теле запроса. Поэтому для обновления ресурса `/bob/bookmarks` вы можете отправить то же самое JSON представление, полученное вызовом `GET`, с измененными значениями полей. Сервис заменит данные.
*   `POST` говорит сервису что-то сделать с данными в теле запроса. Здесь нет никаких жестких правил, но обычно вызов HTTP `POST` для `/bob/bookmarks` будет добавлять значения тела запроса в коллекцию(БД, файловая система, да что угодно)по URI `/bob/bookmarks`. С другой стороны, HTTP `POST` к `/bob/bookmarks/1` может быть обработан так же, как вызовом HTTP `PUT` запроса; сервис возьмет тело запроса и заменит их в ресурсе по переданному URI.

Конечно, некоторые вещи идут не по плану. Скорее всего будет ошибка времени выполнения браузера или сервиса. Мы все получаем раздражающую 404("Страница не найдена") ошибку, когда пытаемся открыть страницу, которая не существует или ссылка на которую неправильна. 404 является кодом статуса. Она передает информацию о состоянии действия. [многие коды статуса](//ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D0%BA%D0%BE%D0%B4%D0%BE%D0%B2_%D1%81%D0%BE%D1%81%D1%82%D0%BE%D1%8F%D0%BD%D0%B8%D1%8F_HTTP) разделены различными диапазонами по назначению. Когда вы делаете запрос к web-странице в браузере, он вызывает HTTP `GET` и если страница отображается, вернется 200 код статуса. 200 означает `OK`; вы можете не знать этого, но это так и есть.

*   Коды статуса в **200x диапазоне**(200-299) указывают, что действие, которое было запрошено клиентом было получено, понято, принято и обработано успешно
*   Коды статуса в **300x диапазоне**(300-399) указывают, что клиент долженпринять дополнительные меры для совершения запроса, такие как перенаправление
*   Коды статуса в **400x диапазоне**(400-499) предназначены для случаев, в которых клиент, возможно, допустил ошибку и должен выполнить корректный запрос. Вышеупомянутый 404 является таким примером
*   Коды статуса в **500x диапазоне**(500-599) предназначены для случаев, когда произошла ошибка на сервере, несмотря на правильность запроса

Создание REST сервиса
---------------------

Во первых, REST сервис закладок должен поддерживать выборку и добавление закладок, а также чтение какой-либо конкретной из них. Ниже представлен первоначальный вид нашего REST сервиса:

`rest/src/main/java/bookmarks/Application.java`

    // tag::runner[]
    package bookmarks;
    
    import java.util.Arrays;
    import java.util.Collection;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.ComponentScan;
    import org.springframework.context.annotation.Configuration;
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
    @ComponentScan
    @EnableAutoConfiguration
    public class Application {
    
    	@Bean
    	CommandLineRunner init(AccountRepository accountRepository,
    			BookmarkRepository bookmarkRepository) {
    		return (evt) -> Arrays.asList(
    				"jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
    				.forEach(
    						a -> {
    							Account account = accountRepository.save(new Account(a,
    									"password"));
    							bookmarkRepository.save(new Bookmark(account,
    									"http://bookmark.com/1/" + a, "A description"));
    							bookmarkRepository.save(new Bookmark(account,
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
    
    	private final AccountRepository accountRepository;
    
    	@RequestMapping(method = RequestMethod.POST)
    	ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {
    		this.validateUser(userId);
    		return this.accountRepository
    				.findByUsername(userId)
    				.map(account -> {
    					Bookmark result = bookmarkRepository.save(new Bookmark(account,
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
    		return this.bookmarkRepository.findOne(bookmarkId);
    	}
    
    	@RequestMapping(method = RequestMethod.GET)
    	Collection<Bookmark> readBookmarks(@PathVariable String userId) {
    		this.validateUser(userId);
    		return this.bookmarkRepository.findByAccountUsername(userId);
    	}
    
    	@Autowired
    	BookmarkRestController(BookmarkRepository bookmarkRepository,
    			AccountRepository accountRepository) {
    		this.bookmarkRepository = bookmarkRepository;
    		this.accountRepository = accountRepository;
    	}
    
    	private void validateUser(String userId) {
    		this.accountRepository.findByUsername(userId).orElseThrow(
    				() -> new UserNotFoundException(userId));
    	}
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    class UserNotFoundException extends RuntimeException {
    
    	public UserNotFoundException(String userId) {
    		super("could not find user '" + userId + "'.");
    	}
    }

`BookmarkRestController` является простым Spring MVC компонентом, аннотированным `@RestController`. `@RestController` представляет аннотированные методы бина как HTTP точки выхода, используя методанные, предоставленные аннотацией `@RequestMapping` к каждому из методов. Метод будет вызываться в том случае, если входящий HTTP запрос соответствует требованиям, предусмотренным аннотацией `@RequestMapping` этого метода.

`@RestController`, когда он расположен на уромне типа, предоставляет значения по умолчанию для всех методов типа. Каждый отдельный метод может переопределять большинство аннотаций на уровне типа. Некоторые вещи **исходят из контекста**. К примеру, `BookmarkRestController` обрабатывает **все** запросы, которые начинаются с имени пользователя(как `bob`), за которым следует `/bookmarks`. Любые методы в типе, которые затем определяются как URI, как `resdBookmark`, добавляются к корневому запросу, что будет соответствовать `/{userId}/bookmarks/{bookmarkId}`. Методы, в которых не указан путь, просто наследуют путь уровня типа. Метод `add` соответствует URI, указанному на уровне типа, но он **только** отвечает на HTTP `POST` запросы.

Слова `{userId}` и `{bookmarkId}` являются переменными пути, подстановочными символами. Spring MVC будет извлекать их из URI и делать их доступными как аргументы под тем именем переменной, которое передается методу контроллера и аннотацией `@PathVariable`. Для HTTP `GET` запроса по URI `/bob/bookmarks/4234` значение агрумента `@PathVariable String userId` будет `"bob"`, а для `@PathVariable Long bookmarkId` будет конвертированное в тип `long` значение `4324`.

Эти методы контроллера возвращают простые POJO - `Collection<Bookmark>` и `Bookmark`. Когда HTTP запрос приходит с указанным заголовком `Accept`, Spring MVC перебирает настройки `HttpMessageConverter` до тех пор, пока не найдет того, кто сможет конвертировать из типов POJO доменной модели в указанный тип заголовка `Accept`. Spring Boot автоматически инициализирует `HttpMessageConverter`, который может конвертировать общий для всех тип `Object` в [JSON](//www.json.org/), при отсутствии каких-либо других указанных конвертеров. `HttpMessageConverter` работает в обоих направлениях: тела входящих запросов конвертируются в Java объекты, а Java объекты конвертируются в тела HTTP ответов.

Используйте `curl`(или ваш браузер), чтобы посмотреть на JSON ответ на запрос `http://localhost:8080/jhoeller/bookmarks`.

Метод `add` указывает на параметр типа `Bookmark` - POJO. Spring MVC будет конвертировать входящий HTTP запрос(содержащий возможно корректный JSON) в POJO с использование соответствующего `HttpMessageConverter`.

Метод `add` принимает входящие HTTP запросы, сохраняет их, а затем отправляет обратнно `ResponseEntity<T>`. `ResponseEntity` является оберткой для ответа и дополнительно для HTTP заголовков и кода статуса. Метод `add` отпрвляет обратно `ResponseEntity` с кодом статуса 201(`CREATED`) и заголовком(`Location`), чтобы клиент мог воспользоваться ими и изучить, как была создана новая запись. Это немного похоже на извлечение первичного ключа после сохранения записи в БД.

Существуют не принимаемые пути. По умолчанию, Spring Boot устанавливает довольно обширный набор реализаций `HttpMessageConverter`, подходящие для использования широкого круга задач, но также легко добавть к нему поддержку и для других, возможно более компактных, эффектиных(для сети) форматов(как [Google Protocol Buffers implementation in Spring 4.1](//docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/converter/protobuf/ProtobufHttpMessageConverter.html)) с использованием обычной Spring MVC конфигурации.

Spring MVC нативно поддерживаетзагрузку файлов через аргументы контроллера типа `MultipartFile multipartFile`.

В Spring MVC это делается простым написанием сервисно-ориентированного кода, в котором фигурирует чистый `HttpServlet` API. Этот код может быть легко протестирован, расширен через Spring AOP. Мы посмотрим, как протестировать эти Spring MVC компоненты в следующей секции.

### Использование HTTP для указания ошибок

Все методы в REST сервисе вызывают `validateUser` для проверки, что запрашиваемый пользователь существует, а если нет, то бросать `UserNotFoundException`. Это исключение содержит Spring MVC аннотацию `@ResponseStatus(HttpStatus.NOT_FOUND)`, которая говорит Spring MVC отправлять обратно код HTTP статуса(404) при каждом срабатывании исключения. Это действительно хороший прием: вы можете думать в терминах вашего бизнес домена в вашем коде и вы получите умную обработку, которая прекрасно соответствует тому, как HTTP указывает на ошибки клиента, используя коды статуса. Spring MVC предоставляет множество различных мест, чтобы аккуратно [обработать ошибки](//spring.io/blog/2013/11/01/exception-handling-in-spring-mvc).

Тестирование REST сервиса
-------------------------

Spring MVC предоставляет хорошоую поддержку [для тестирования HTTP точек выхода](//docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/testing.html#spring-mvc-test-framework). Он предоставляет очень хорошее нечто среднее между unit-тестированием и интеграционным тестированием в том, что он позволяет вам поднять весь Spring MVC механизм на основе `DispatcherServlet`, включая валидаторы, `HttpMessageConverter` и многое другое, а затем запустить тесты без фактического запуска самого HTTP сервиса: как убить двух зайцев одновременно! От интеграционного тестирования в том, что вы заботитесь о фактической реализации логики, от unit-тестирования - в том, что вы не дожидаетесь инициализации web-сервера и начинаете обслуживать запросы.

ниже показан unit-тест для `ReservationRestController`. Он должен быть знаком каждому, кто писал до этого тесты на JUnit. На верху мы используем аннотацию `@SpringApplicationConfiguration(classes = Application.class)`(из Spring Boot), чтобы сказать `SpringJUnit4ClassRunner`, где он должен взять информацию о Spring приложении для тестирования. Аннотация `@WebAppConfiguration` говорит JUnit, что это unit-тест для Spring MVC web-компонентов и поэтому должен выполняться под `WebApplicationContext`, а не стандартной `ApplicationContext` реализации.

`rest/src/test/java/bookmarks/BookmarkRestControllerTest.java`

    package bookmarks;
    
    import org.junit.Assert;
    import org.junit.Before;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.SpringApplicationConfiguration;
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
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = Application.class)
    @WebAppConfiguration
    public class BookmarkRestControllerTest {
    
    
        private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
                MediaType.APPLICATION_JSON.getSubtype(),
                Charset.forName("utf8"));
    
        private MockMvc mockMvc;
    
        private String userName = "bdussault";
    
        private HttpMessageConverter mappingJackson2HttpMessageConverter;
    
        private Account account;
    
        private List<Bookmark> bookmarkList = new ArrayList<>();
    
        @Autowired
        private BookmarkRepository bookmarkRepository;
    
        @Autowired
        private WebApplicationContext webApplicationContext;
    
        @Autowired
        private AccountRepository accountRepository;
    
        @Autowired
        void setConverters(HttpMessageConverter<?>[] converters) {
    
            this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                    hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();
    
            Assert.assertNotNull("the JSON message converter must not be null",
                    this.mappingJackson2HttpMessageConverter);
        }
    
        @Before
        public void setup() throws Exception {
            this.mockMvc = webAppContextSetup(webApplicationContext).build();
    
            this.bookmarkRepository.deleteAllInBatch();
            this.accountRepository.deleteAllInBatch();
    
            this.account = accountRepository.save(new Account(userName, "password"));
            this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + userName, "A description")));
            this.bookmarkList.add(bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + userName, "A description")));
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
                    this.account, "http://spring.io", "a bookmark to the best resource for Spring news and information"));
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

Во-первых, на что нужно обратить внимание - это метод `setup`, аннотированный `@Before`. Этот метод инициализирует `MockMvc`, которому требуется ссылка на `WebApplicationContext` приложения. `MockMvc` является ключевой фигурой: все тесты будут постоянно использовать переменную типа `MockMvc` для имитации HTTP запросов к сервису. И... это всё! Посмотрите на любой другой из тестов. Мы можем использовать статический импорт из `org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*` для сцепления вместе HTTP запросов и проверки их ответов.

Для всех тестов указаны `application/json` `content-type` и ответы ожидаются такого же типа. Тесты используют метод `MockMvcResultMatchers#jsonPath` для проверки структуры и содержания JSON ответов, что в свою очередь использует Jayway JSON Path API для запуска обхода с тиле X-Path по JSON структурам, как мы делаем в различных частях unit-тестов.

Тестирование приложения через Postman
-----------------------------

 ![](2024-10-19_16-12-07.png)
  ![](2024-10-19_16-12-31.png)
   ![](2024-10-19_16-13-00.png)
    ![](2024-10-19_16-21-43.png)


# Дополнительный материал (не входит в РПД СТП и в курсовой не требуется)

Создание HATEOAS REST сервиса
-----------------------------

Текущий первоначальный вид API работает очень хорошо. Если этот сервис хорошо документирован, он будет работоспособен для REST клиентов написанных на множестве различных языков. Это чистый API в том, что мспользуются некоторые из примитивов, которые предоставляет HTTP, понятным способом. Особенностью API является соответствие [принципу единого интерфейса](//en.wikipedia.org/wiki/Representational_state_transfer#Uniform_interface). Каждое сообщение включает достаточно информации для описания того, как обрабатывать сообщение. К примеру, клиент может решить, какой парсер будет вызван на основе заголовка `Content-type` в сообщении запроса. Состояние системы соответствует идентификации уникальности URI ресурса. Изменение состояния осуществляется через HTTP глаголы (`POST`, `GET`, `DELETE`, `PUT` и др.). Таким образом, когда клиент хранит представление ресурса, включая метаданные, он имеет достаточно информации для изменения или удаления ресурса.

Но мы можем сделать лучше. Сервисы остаются соответствовать задачам, но отсутствует... **надежность**. Как сказано в Википедии: Клиенты должны знать API. Изменения в API отталкивают клиентов и они не обращаются к документации сервиса. Гипермедиа как двигатель состояния приложения(a.k.a. [HATEOAS](//en.wikipedia.org/wiki/HATEOAS)) является ещё одним ограничением, который адресован и удаляет эту нестыковку. Клиенты создают состояния переходов только через действия, которые динамически идентифицируются в гипермедиа сервером.

Давайте посмотрим на исправленный вариант API, который показан ниже, но на этот раз с использованием [Spring HATEOAS](https://spring-projects.ru/projects/spring-hateoas/). Это сделано для того, чтобы рассказать, что Spring HATEOAS делает проще предоставление ссылок, матаданных о полезной нагрузке, которая будет возвращена клиенту. В принципе, все что мы будем делать, это обертывать наш ответ полезной нагрузкой, используя Spring HATEOAS тип `ResourceSupport`. Этот тип аккумулирует объекты `Link`, которые описывают ресурсы. К примеру, ресурс описывает счет в e-commerce решении, который может иметь ссылку на ресурс заказа по счету, ссылку на текущую карту оплаты и ссылку, которая может быть использована для повторного извлечения состояния ресурса.

`hateoas/src/main/java/bookmarks/Application.java`

    package bookmarks;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.ComponentScan;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.hateoas.Link;
    import org.springframework.hateoas.ResourceSupport;
    import org.springframework.hateoas.Resources;
    import org.springframework.hateoas.VndErrors;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    
    import java.net.URI;
    import java.util.Arrays;
    import java.util.List;
    import java.util.stream.Collectors;
    
    import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
    import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
    
    @Configuration
    @ComponentScan
    @EnableAutoConfiguration
    public class Application {
    
        @Bean
        CommandLineRunner init(AccountRepository accountRepository, BookmarkRepository bookmarkRepository) {
            return (evt) ->
                    Arrays.asList("jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
                            .forEach(a -> {
                                Account account = accountRepository.save(new Account(a, "password"));
                                bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/1/" + a, "A description"));
                                bookmarkRepository.save(new Bookmark(account, "http://bookmark.com/2/" + a, "A description"));
                            });
        }
    
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }
    
    class BookmarkResource extends ResourceSupport {
    
        private final Bookmark bookmark;
    
        public BookmarkResource(Bookmark bookmark) {
            String username = bookmark.getAccount().getUsername();
            this.bookmark = bookmark;
            this.add(new Link(bookmark.getUri(), "bookmark-uri"));
            this.add(linkTo(BookmarkRestController.class, username).withRel("bookmarks"));
            this.add(linkTo(methodOn(BookmarkRestController.class, username).readBookmark(username, bookmark.getId())).withSelfRel());
        }
    
        public Bookmark getBookmark() {
            return bookmark;
        }
    }
    
    @RestController
    @RequestMapping("/{userId}/bookmarks")
    class BookmarkRestController {
    
        private final BookmarkRepository bookmarkRepository;
    
        private final AccountRepository accountRepository;
    
        @RequestMapping(method = RequestMethod.POST)
        ResponseEntity<?> add(@PathVariable String userId, @RequestBody Bookmark input) {
    
            this.validateUser(userId);
    
            return accountRepository.findByUsername(userId)
                    .map(account -> {
                                Bookmark bookmark = bookmarkRepository.save(new Bookmark(account, input.uri, input.description));
    
                                HttpHeaders httpHeaders = new HttpHeaders();
    
                                Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");
                                httpHeaders.setLocation(URI.create(forOneBookmark.getHref()));
    
                                return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
                            }
                    ).get();
        }
    
        @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
        BookmarkResource readBookmark(@PathVariable String userId, @PathVariable Long bookmarkId) {
            this.validateUser(userId);
            return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
        }
    
    
        @RequestMapping(method = RequestMethod.GET)
        Resources<BookmarkResource> readBookmarks(@PathVariable String userId) {
    
            this.validateUser(userId);
    
            List<BookmarkResource> bookmarkResourceList = bookmarkRepository.findByAccountUsername(userId)
                    .stream()
                    .map(BookmarkResource::new)
                    .collect(Collectors.toList());
            return new Resources<BookmarkResource>(bookmarkResourceList);
        }
    
        @Autowired
        BookmarkRestController(BookmarkRepository bookmarkRepository,
                               AccountRepository accountRepository) {
            this.bookmarkRepository = bookmarkRepository;
            this.accountRepository = accountRepository;
        }
    
        private void validateUser(String userId) {
            this.accountRepository.findByUsername(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
        }
    }
    
    @ControllerAdvice
    class BookmarkControllerAdvice {
    
        @ResponseBody
        @ExceptionHandler(UserNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        VndErrors userNotFoundExceptionHandler(UserNotFoundException ex) {
            return new VndErrors("error", ex.getMessage());
        }
    }
    
    
    class UserNotFoundException extends RuntimeException {
    
        public UserNotFoundException(String userId) {
            super("could not find user '" + userId + "'.");
        }
    }

Эти `Link` соответствуют тем же элементам `<link/>`, которые часто используются в HTML страницах и CSS таблицах. Они имеют атрибуты `href` и `rel`. Атрибут `href` указывает на местоположение CSS таблицы, а `rel` говорит клиенту(браузеру) почему этот ресурс важен(потомучто эта таблица используется для отображения страницы). Поэтому не составляет труда перевести элемент `link` в JSON.

`BookmarkResource` обертывает `Bookmark` и предоставляет хорошее сентрализованное место для хранения логики для построения связей. Как минимум, ресурс должен предоставлять ссылку на самого себя(обычно это ссылка, где значением атрибута `rel` является `self`). Мы могли бы просто написать [http://127.0.0.1:8080/\\{userId\\}/bookmarks/\\{id\\}](//127.0.0.1:8080/%7Buserid%7D/bookmarks/%7Bid%7D), заменяя значения в адресе соответствующими значениями, но это будет ошибкой, т.к. мы обращаемся к другому хосту, порту и корню контекста. Мы можем использовать `ServletUriComponentsBuilder` для упрощения некоторой работы. Но почему мы должны? В конце концов, Spring MVC уже знает об этом URI. Он содержится в `@RequestMapping` в каждом методе контроллера! Spring HATEOAS предоставляет удобные статические методы `ControllerLinkBuilder.linkTo` и `ControllerLinkBuilder.methodOn` для извлечения URI из метаданных контроллера - заметное улкчшение и следование принципам DRY (do not repeat yourself). Пример показывает как создать объект `Link` непосредственно, выбрав произвольное значение для `href`(в данном случае, URI закладки на саму себя), как создать ссылку на основе метаданных `@RequestMapping` Spring MVC контроллера, либо конкретного метода контроллера.

На данном этапе остается только заменить `Bookmark` на `BookmarkResource`.

### Улучшенная обработка ошибок с использованием `VndErrors`

HATEOAS передает клиентам улучшенные метаданные о самом сервисе. Мы можем также улучшить ситуацию обработки ошибок. Коды HTTP статусов 400-499, к примеру, говорят клиенту, что клиент сделал что-то неправильнное, коды 500-599 говорят о чем-то нехорошем на стороне сервера. Если вы знаете ваши коды статусов, то это может быть началом к пониманию, как работать с API. Но мы можем сделать лучше. В конце концов, перед тем, как клиент запустится, кому-то необходимо разработать его и сообщения об ошибках могут быть бесценными для понимания API. Ошибки могут быть обработаны последовательным способом даже лучше!

[VndError](//github.com/blongden/vnd.error) является популярным, де-факто стандартом mime-типом для описания кодировки ошибок клиента REST сервиса. Он работает также с ошибками 40х и 50х. Spring HATEOAS предоставляет типы `VndError`, которые вы можете использовать для сообщения об ошибке обратнно клиенту.

В наш исправленный сервис вводится новый класс `BookmarkControllerAdvice`, который использует Spring MVC аннотацию `@ControllerAdvice`. Использование этой аннотации является полезным способом для распределения общих проблем конфигурации, как обработка ошибки, в отдельных местах, отдельно от конкретного Spring MVC контроллера. Spring MVC, к примеру, определяет `@ExceptionHandler` методу, который привязывает определенный метод обработчика к любому вызову `Exception` или коду HTTP статуса. Здесь мы говорим Spring MVC, что любой код, который бросает `UserNotFoundException`, как и ранее, должен быть обработан методом `userNotFoundExceptionHandler`. Этот метод просто оборачивает `Exception` в `VndErrors` и возвращает его клиенту. В этом примере мы убрали аннотацию `@ResponseStatus` из исключения и объединили в `@ControllerAdvice`. `@ControllerAdvice` является удобным способом для объединения логики всех видов и, в отличие от аннотированных типов исключений, возможно использование для каждого из типов исключения, к которому у вас нет исходного кода.

Обеспечение защиты REST сервиса
-------------------------------

До сих пор мы исходили из предположения, что все клиенты надежны и что они должны обладать полным доступом ко всем данным. На самом деле такое редко бывает. Открытое REST API незащищено. Хотя это нетрудно исправить. [Spring Security](https://spring-projects.ru/projects/spring-security/) предоставляет фундамент для защиты доступа к приложению. Spring Security необходимо иметь представление о пользователях вашего приложения и их правах. Эти права отвечают на вопрос: что может пользователь приложения видеть или делать?

Основой Spring Security является интерфейс `UserDetailsService`, который выполняет одну задачу: по переданному имени пользователя, исполняет реализацию `UserDetails`, которая должна отвечать на запросы актуальности учетной записи, её пароля, имени пользователя и прав(представленными экземплярами `org.springframework.security.core.GrantedAuthority`).

    package org.springframework.security.core.userdetails;
    
    public interface UserDetailsService {
    
        org.springframework.security.core.userdetails.UserDetails loadUserByUsername(java.lang.String s)
            throws org.springframework.security.core.userdetails.UsernameNotFoundException;
    
    }

Spring Security предоставляет множество реализаций для адаптации существующих поставщиков идентификации, таких как Active Directory, LDAP, `pam`, CAAS и др. [Spring Social](https://spring-projects.ru/projects/spring-social/) предоставляет хорошую интеграцию с такими сервисами аутентификации на основе OAuth, как Facebook, Twitter и др.

Наш пример уже имеет понятие `Account`, поэтомы мы можем просто приспособить его к нашей реализации `UserDetailsService`, как показано ниже в классе `WebSecurityConfiguration`. Spring Security будет опрашивать этот `UserDetailsService`, есть ли у него какой-либо ответ на запрос аутентификации.

### Авторизация и аутентификация клиента в Open Web с использование Spring Security OAuth

Мы можем выпонить запросы аутентификации клиента бесчисленным количеством способов. Клиенты могут отправлять, к примеру имя пользователя и пароль в каждом HTTP-запросе. Они могут передавать х509 сертификат в каждом запросе. В действительности существует множество подходов, которые могут быть использованы здесь.

Наш API предназначен для работы поверх open-web. Он предназначен для использования всевозможных HTML5 и нативных мобильных и настольных клиентов, которые мы намерены создать. Мы будем использовать разнообразные клиенты с различной степенью защиты и любое решение какое бы мы не выбрали, должно соответствовать этому. Мы должны также отделить имя пользователя и пароль из сессии приложения. В итоге, если сбросить пароль на Twitter, не хочется, чтобы клиенту пришлось повторно аутентифицироваться. С другой стороны, если кто-то угонит его у одного из наших клиентов(к примеру, если пользователь потерял телефон), то мы не хотим, чтобы злоумышленник смог заблокировать нашего пользователя.

[OAuth](//oauth.net/) предоставляет прозрачный способ обработки этих проблем. Без сомнения, вы уже использовали OAuth. Одним из распостраненных случаев - это когда устанавливается Facebook плагин или игра. Обычно он состоит из следующих этапов:

*   пользователь находит игру или часть функциональности, которой требуется доступ данным пользователя, к примеру, "Sign in With Facebook"
*   пользователь нажимает "install" или "add" и затем перенаправляется на доверенный домен(Facebook.com), где пользователю будет предложено предоставить определенные разрешения
*   пользователь подтверждает эти разрешения и впоследствии перенаправляется обратно к приложению с полученным ключом. Оно будет использовать этот ключ в запросах к Facebook от вашего имени

В этом примере любой старый клиент может обратиться к Facebook, а по окончании иметь ключ доступа. Этот ключ доступа передается через все последующие REST запросы, наподобие HTTP cookie. Имя пользователи и пароль не нужно будет передавать и клиент может кешировать ключ доступа на определенный или неопределенный период. Пользователям клиента нет необходимости повторно аутентифицироваться каждый раз при открытии приложения. А ещё лучше, ключ доступа определять к каждому клиенту. Это может быть использовано для определения того, что одному клиенту требуется больше разрешений, чем другим. В этом и есть преимущество, состоящее в том, что любая конфиденциальная информация, как имя пользователь и пароль, никогда не будет в открытом виде у ненадежных приложений, которые могут использовать её в злонамеренных целях. Мы можем быть уверены в том, что любой клиент будет дружественным. OAuth поддерживает простейший этап проверки подлинности пользователя( обычно отправляя имя пользователя и пароль) от клиента и сервис возвращает ключ доступа OAuth без необходимости перенаправления на доверенный домен. Этот подход мы и возьмем: результатом будет то, что наши клиенты будут иметь ключ доступа, который отделен от имени пользователя и пароля, и который может быть использован для присвоения различных уровней защиты различным клиентам.

Настройка OAuth для вашего приложения проста. Конфигурационный класс `OAuth2Configuration` описывает один клиент(здесь гипотетический Android клиент), которому необзодим `ROLE_USER` и `write`. Spring Security OAuth будет читать эту информацию из `AuthenticationManager`, который к итоге настроен на использовании нашей собственной `UserDetailsService` реализации.

OAuth очень гибок. Вы можете, к примеру, развернуть сервер авторизации, общий для множества REST API. В этом случае, наша OAuth реализация живет рядом с нашим REST API закладок, т.к. они одни и те же. Именно поэтому мы использовали оба `@EnableResourceServer` и `@EnableAuthorizationServer` в одном и том же конфигурационном классе.

### Вы сказали что-то про Open Web?

Мы ожидаем, что некоторые из клиентов нашего сервиса будут на HTML5. Они будут обращаться к нашему REST API с различных доменов, а в большинстве браузеров это идет в разрез политики безопасности cross-site-scripting. Мы можем явно включить XSS для заранее известных клиентов, вставляя CORS (cross-origin request scripting) заголовки. Эти заголовки, если они присутствуют в ответах сервиса, указывают браузеру, что запросы происхождения, форм и конфигурации описаны в заголовках, разрешенных даже в различных доменах. Наш API оформлен простым `javax.servlet.Filter`, который добавляет эти заголовки в каждый запрос. В примере мы делегируем свойству `tagit.origin`, если предоставлен или по умолчанию `http://localhost:9000`, где у нас может, к примеру, JavaScript клиент осуществлять запросы к сервису. Мы могли бы легко прочитать эту информацию из хранилица. Мы указали только один источник, но можно также легко указать различное количество клиентов.

### Использование HTTPS (SSL/TLS) для предотвращения Man-in-the-Middle атак

Spring Boot предоставляет встроенный web-сервер(Apache Tomcat по умолчанию), который может быть настроен программно делать что-либо, что и отдельный Apache Tomcat web-сервер может делать. Раньше требовалось выполнить несколько нудных шагов для настройки HTTPS (SSL/TLS). Теперь, Spring Boot делает это супер просто. Для начала, создайте следующее:

`security/src/main/resources/application-https.properties`

\# Настройка сервера на запуск с SSL/TLS и использования HTTPS
server.port = 8443
server.ssl.key-store = classpath:tomcat.keystore
server.ssl.key-store-password = password
server.ssl.key-password = password

Этот файл свойств активируется, только когда приложение запущено и **SPRING\_PROFILES\_ACTIVE** настроен с профилем **https**.

HTTPS требует подписанный сертификат, который мы предъявляем, используя значения свойств. Для этого мы можем использовать из JDK **keytool**:

$ keytool -genkey -alias bookmarks -keyalg RSA -keystore src/main/resources/tomcat.keystore
Enter keystore password: password
Re-enter new password: password
What is your first and last name?
  \[Unknown\]:  Josh Long
What is the name of your organizational unit?
  \[Unknown\]:  Spring Team
What is the name of your organization?
  \[Unknown\]:  Pivotal
What is the name of your City or Locality?
  \[Unknown\]:  IoT
What is the name of your State or Province?
  \[Unknown\]:  Earth
What is the two-letter country code for this unit?
  \[Unknown\]:  US
Is CN=Josh Long, OU=Spring Team, O=Pivotal, L=IoT, ST=Earth, C=US correct?
  \[no\]:  yes

Enter key password for <learningspringboot>
	(RETURN if same as keystore password): <RETURN>

Файлы ключей ДОЛЖНЫ быть в файловой системе втроенного Tomcat, чтобы прочесть их. Они НЕ могут быть в виде JAR файлов.

Создадуться хранилище ключей в `src/main/resources`. Теперь, если вы запустите приложение, то оно будет использовать хранилище ключей для запуска встроенного контейнера сервлетов Tomcat с использованием SSL/TLS.

как говорилось ранее, этот режим доступен только для использования профиля Spring `https`. Это означает, что вы можете запускать и тестировать приложение **без** примененного профиля, либо также просто переключившись на него.

### Собираем все вместе

`security/src/main/java/bookmarks/Application.java`

    package bookmarks;
    
    import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
    import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
    
    import java.io.IOException;
    import java.net.URI;
    import java.security.Principal;
    import java.util.Arrays;
    import java.util.List;
    import java.util.stream.Collectors;
    
    import javax.servlet.Filter;
    import javax.servlet.FilterChain;
    import javax.servlet.FilterConfig;
    import javax.servlet.ServletException;
    import javax.servlet.ServletRequest;
    import javax.servlet.ServletResponse;
    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
    import org.springframework.boot.context.embedded.FilterRegistrationBean;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.ComponentScan;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.hateoas.Link;
    import org.springframework.hateoas.ResourceSupport;
    import org.springframework.hateoas.Resources;
    import org.springframework.hateoas.VndErrors;
    import org.springframework.http.HttpHeaders;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
    import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.AuthenticationException;
    import org.springframework.security.core.authority.AuthorityUtils;
    import org.springframework.security.core.userdetails.User;
    import org.springframework.security.core.userdetails.UserDetailsService;
    import org.springframework.security.core.userdetails.UsernameNotFoundException;
    import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
    import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
    import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
    import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
    import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
    import org.springframework.web.bind.annotation.ControllerAdvice;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.ResponseBody;
    import org.springframework.web.bind.annotation.ResponseStatus;
    import org.springframework.web.bind.annotation.RestController;
    
    //
    // curl -X POST -vu android-bookmarks:123456 http://localhost:8080/oauth/token -H "Accept: application/json" -d "password=password&username=jlong&grant_type=password&scope=write&client_secret=123456&client_id=android-bookmarks"
    // curl -v POST http://127.0.0.1:8080/tags --data "tags=cows,dogs"  -H "Authorization: Bearer 66953496-fc5b-44d0-9210-b0521863ffcb"
    
    @Configuration
    @ComponentScan
    @EnableAutoConfiguration
    public class Application {
    
    	// CORS
    	@Bean
    	FilterRegistrationBean corsFilter(
    			@Value("${tagit.origin:http://localhost:9000}") String origin) {
    		return new FilterRegistrationBean(new Filter() {
    			public void doFilter(ServletRequest req, ServletResponse res,
    					FilterChain chain) throws IOException, ServletException {
    				HttpServletRequest request = (HttpServletRequest) req;
    				HttpServletResponse response = (HttpServletResponse) res;
    				String method = request.getMethod();
    				// this origin value could just as easily have come from a database
    				response.setHeader("Access-Control-Allow-Origin", origin);
    				response.setHeader("Access-Control-Allow-Methods",
    						"POST,GET,OPTIONS,DELETE");
    				response.setHeader("Access-Control-Max-Age", Long.toString(60 * 60));
    				response.setHeader("Access-Control-Allow-Credentials", "true");
    				response.setHeader(
    						"Access-Control-Allow-Headers",
    						"Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
    				if ("OPTIONS".equals(method)) {
    					response.setStatus(HttpStatus.OK.value());
    				}
    				else {
    					chain.doFilter(req, res);
    				}
    			}
    
    			public void init(FilterConfig filterConfig) {
    			}
    
    			public void destroy() {
    			}
    		});
    	}
    
    	@Bean
    	CommandLineRunner init(AccountRepository accountRepository,
    			BookmarkRepository bookmarkRepository) {
    		return (evt) -> Arrays.asList(
    				"jhoeller,dsyer,pwebb,ogierke,rwinch,mfisher,mpollack,jlong".split(","))
    				.forEach(
    						a -> {
    							Account account = accountRepository.save(new Account(a,
    									"password"));
    							bookmarkRepository.save(new Bookmark(account,
    									"http://bookmark.com/1/" + a, "A description"));
    							bookmarkRepository.save(new Bookmark(account,
    									"http://bookmark.com/2/" + a, "A description"));
    						});
    	}
    
    	public static void main(String[] args) {
    		SpringApplication.run(Application.class, args);
    	}
    }
    
    @Configuration
    class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
    
    	@Autowired
    	AccountRepository accountRepository;
    
    	@Override
    	public void init(AuthenticationManagerBuilder auth) throws Exception {
    		auth.userDetailsService(userDetailsService());
    	}
    
    	@Bean
    	UserDetailsService userDetailsService() {
    		return (username) -> accountRepository
    				.findByUsername(username)
    				.map(a -> new User(a.username, a.password, true, true, true, true,
    						AuthorityUtils.createAuthorityList("USER", "write")))
    				.orElseThrow(
    						() -> new UsernameNotFoundException("could not find the user '"
    								+ username + "'"));
    	}
    }
    
    @Configuration
    @EnableResourceServer
    @EnableAuthorizationServer
    class OAuth2Configuration extends AuthorizationServerConfigurerAdapter {
    
    	String applicationName = "bookmarks";
    
    	// This is required for password grants, which we specify below as one of the
    	// {@literal authorizedGrantTypes()}.
    	@Autowired
    	AuthenticationManagerBuilder authenticationManager;
    
    	@Override
    	public void configure(AuthorizationServerEndpointsConfigurer endpoints)
    			throws Exception {
    		// Workaround for https://github.com/spring-projects/spring-boot/issues/1801
    		endpoints.authenticationManager(new AuthenticationManager() {
    			@Override
    			public Authentication authenticate(Authentication authentication)
    					throws AuthenticationException {
    				return authenticationManager.getOrBuild().authenticate(authentication);
    			}
    		});
    	}
    
    	@Override
    	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    
    		clients.inMemory().withClient("android-" + applicationName)
    				.authorizedGrantTypes("password", "authorization_code", "refresh_token")
    				.authorities("ROLE_USER").scopes("write").resourceIds(applicationName)
    				.secret("123456");
    	}
    }
    
    class BookmarkResource extends ResourceSupport {
    
    	private final Bookmark bookmark;
    
    	public BookmarkResource(Bookmark bookmark) {
    		String username = bookmark.getAccount().getUsername();
    		this.bookmark = bookmark;
    		this.add(new Link(bookmark.uri, "bookmark-uri"));
    		this.add(linkTo(BookmarkRestController.class, username).withRel("bookmarks"));
    		this.add(linkTo(
    				methodOn(BookmarkRestController.class, username).readBookmark(null,
    						bookmark.getId())).withSelfRel());
    	}
    
    	public Bookmark getBookmark() {
    		return bookmark;
    	}
    }
    
    @RestController
    @RequestMapping("/bookmarks")
    class BookmarkRestController {
    
    	private final BookmarkRepository bookmarkRepository;
    
    	private final AccountRepository accountRepository;
    
    	@RequestMapping(method = RequestMethod.POST)
    	ResponseEntity<?> add(Principal principal, @RequestBody Bookmark input) {
    		String userId = principal.getName();
    		this.validateUser(userId);
    
    		return accountRepository
    				.findByUsername(userId)
    				.map(account -> {
    					Bookmark bookmark = bookmarkRepository.save(new Bookmark(account,
    							input.uri, input.description));
    
    					HttpHeaders httpHeaders = new HttpHeaders();
    
    					Link forOneBookmark = new BookmarkResource(bookmark).getLink("self");
    					httpHeaders.setLocation(URI.create(forOneBookmark.getHref()));
    
    					return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    				}).get();
    	}
    
    	@RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    	BookmarkResource readBookmark(Principal principal, @PathVariable Long bookmarkId) {
    		String userId = principal.getName();
    		this.validateUser(userId);
    		return new BookmarkResource(this.bookmarkRepository.findOne(bookmarkId));
    	}
    
    	@RequestMapping(method = RequestMethod.GET)
    	Resources<BookmarkResource>readBookmarks(Principal principal) {
    		String userId = principal.getName();
    		this.validateUser(userId);
    
    		List<BookmarkResource> bookmarkResourceList = bookmarkRepository
    				.findByAccountUsername(userId).stream().map(BookmarkResource::new)
    				.collect(Collectors.toList());
    		return new Resources<BookmarkResource>(bookmarkResourceList);
    	}
    
    	@Autowired
    	BookmarkRestController(BookmarkRepository bookmarkRepository,
    			AccountRepository accountRepository) {
    		this.bookmarkRepository = bookmarkRepository;
    		this.accountRepository = accountRepository;
    	}
    
    	private void validateUser(String userId) {
    		this.accountRepository.findByUsername(userId).orElseThrow(
    				() -> new UserNotFoundException(userId));
    	}
    }
    
    @ControllerAdvice
    class BookmarkControllerAdvice {
    
    	@ResponseBody
    	@ExceptionHandler(UserNotFoundException.class)
    	@ResponseStatus(HttpStatus.NOT_FOUND)
    	VndErrors userNotFoundExceptionHandler(UserNotFoundException ex) {
    		return new VndErrors("error", ex.getMessage());
    	}
    }
    
    @SuppressWarnings("serial")
    class UserNotFoundException extends RuntimeException {
    
    	public UserNotFoundException(String userId) {
    		super("could not find user '" + userId + "'.");
    	}
    }

С Spring Security OAuth мы можем немного доработать наш API. Не имеет смысла иметь разбросанные `userId` по всему URI, потому что контекст пользователя предполагает безопасный доступ к нашим точкам выхода. Вместо требования `userId` в пути, безопасные методы обработчика Spring MVC ожидают `javax.security.Principal`, который Spring Security внедряет от нашего имени. Он имеет метод `javax.security.Principal#getName`, который можно использовать вместо `userId`.

Заключение
----------

REST является наиболее естесственным способом для повсеместного, распределенного взаимодействия с клиентами. Он работает потому что работает HTTP. Когда вы поймете, как REST встраивается в приложение, тогда вам не трудно будет представить архитектуру с многочисленными, отдельновзятыми API, открытыми поверх REST, с балансировкой нагрузки, каким и должен быть любой другой HTTP сервис. Не удивительно, что REST(часто, но не обязательно) образует важнейшую основу для микросервисов, которые рассмотрены на протяжении всего урока.

С оригинальным текстом урока вы можете ознакомиться на [spring.io](//spring.io/guides/tutorials/bookmarks/).

