# Spring Boot Rest Api Data JPA OneToOne - Playlists
Dans ce tutoriel, nous allons écrire un service CRUD REST Spring Boot basé sur un mappage unidirectionnel d'une clé étrangère pour une relation JPA et Hibernate OneToOne et entièrement couvert par des tests.

La relation OneToOne fait référence à la relation entre deux tables :

* **Song** : a un identifiant, un titre, une description, une catégorie, une durée et le nom d'un artiste.

* **Melody** : a un identifiant, un pitch, une durée et un type

Une Chanson peut être liée à une seule mélodie et vice versa.

Apis aide à créer, récupérer, mettre à jour, supprimer des Song et des melody.

Apis prend également en charge les méthodes de recherche personnalisées (query methods) telles que la recherche par catégorie ou par le nom de l’artiste ou par type de melody.

##### La relation OneToOne
– L’annotation **@OneToOne** définit une relation 1:1 entre deux entités.

– L’annotation **@JoinColumn**  définit une colonne de clé étrangère

##### Spring Boot
Spring Boot est un projet Spring qui facilite le processus de configuration et de publication des applications.

En suivant des étapes simples, vous pourrez exécuter votre premier projet.

##### API REST (Representational State Transfer Application Program Interface)
Il se base sur le protocole **HTTP** pour transférer des informations. 
Un client lance une requête HTTP, et le serveur renvoie une réponse à travers plusieurs méthodes dont les plus utilisées sont : **POST**, **GET**, **PUT** et **DELETE**.

##### Outils utilisés : 
* Java 8
* IDE Intellij IDEA
* Spring Boot 2.5.7 (avec Spring Web MVC et Spring Data JPA)
* PostgreSQL
* H2 Database
* Lombok 1.18.22
* Maven 4.0.0


## Initialisation du projet
Pour amorcer une application Spring Boot , nous pouvons démarrer le projet à partir de zéro avec notre IDE préféré, ou simplement utiliser un autre moyen qui facilite la vie : [SpringInitializr](https://start.spring.io/)

Initialement, nous avons choisi les dépendances suivantes : Spring web, Spring Data JPA, Validation, H2 Database, Lombok et PostgreSQL Driver.

![Screenshot](src/main/resources/images/springInitializer.PNG)

## Structure du projet
L'image ci-dessous montre la structure finale du projet

![Screenshot](src/main/resources/images/structure-projet.png)

* **Pom.xml**

Contient des dépendances pour Spring Boot. Dans notre cas, nous avons besoin de ces dépendances.

```xml
<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
			<version>2.5.7</version>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.postgresql</groupId>
			<artifactId>postgresql</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
        <dependency>
            <groupId>io.springfox</groupId>
            <artifactId>springfox-swagger2</artifactId>
            <version>2.9.2</version>
        </dependency>
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger-ui</artifactId>
			<version>2.9.2</version>
		</dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.9</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
		<dependency>
			<groupId>org.hibernate</groupId>
			<artifactId>hibernate-envers</artifactId>
			<version>5.6.1.Final</version>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<version>1.18.22</version>
		</dependency>
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>
```

* **Main Class**

C’est la classe principale de l’application et appelée aussi une classe de démarrage.

L ’adresse par défaut d’exécution : http://localhost:8080 

```java 
@SpringBootApplication
public class PlaylistApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlaylistApplication.class, args);
	}

}
```

## I. Configuration PostgreSQL
* **application.properties**

Les propriétés **spring.datasource.username** et **spring.datasource.password** sont les mêmes que celles de votre installation de base de données.

Spring Boot utilise Hibernate pour l'implémentation JPA, nous configurons PostgreSQLDialect pour PostgreSQL 🡺 Ce dialecte nous permet de générer de meilleures requêtes SQL pour cette base de données.

**spring.jpa.hibernate.ddl-auto= update** est utilisé pour créer automatiquement les tables en fonction des classes d’entités dans l’application. Toute modification du modèle déclenche également une mise à jour de la table. 

Pour la production, cette propriété doit être **validate**, cette valeur valide le schéma en correspondance avec le mapping hibernate.


```yaml
spring.datasource.url=jdbc:postgresql://localhost:5432/playlist_song_melody_db
spring.datasource.username=playlistadmin
spring.datasource.password=admin

spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect

spring.jpa.hibernate.ddl-auto=update
```
## II. Modèle
* **AbstractAuditModel**
Les deux modèles de l’application Melody et Song auront des champs communs liés à l'audit tels que createdAt et updatedAt.

Il est préférable de faire abstraction de ces champs communs dans une classe de base distincte appelée AbstractAuditModel. Cette classe sera étendue par d'autres entités.

**@EntityListeners(AuditingEntityListener.class)** : les valeurs de createdAt et updatedAt seront automatiquement renseignées lorsque les entités seront conservées.

**@MappedSuperclass.java**

En utilisant la stratégie MappedSuperclass, l'héritage n'est évident que dans la classe mais pas dans le modèle d'entité. Il faut noter que cette classe n'a plus d’annotation @Entity, car elle ne sera pas conservée seule dans la base de données.

```java
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class AbstractAuditModel implements Serializable {

    @CreatedDate
    @JsonIgnore
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createAt = Instant.now();

    @LastModifiedDate
    @JsonIgnore
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Instant getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Instant createAt) {
        this.createAt = createAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```
**@EnableJpaAuditing** : Pour activer l'audit JPA (dans la classe de repository)

* **Song.java**

L’entité « Song » est mappée à une table nommée « songs » dans la base de données

– l'annotation **@Entity** indique que la classe est une classe Java persistante.

– l'annotation **@Table** fournit la table qui mappe cette entité.

– l'annotation **@Id** est pour la clé primaire.

– l'annotation **@GeneratedValue** est utilisée pour définir la stratégie de génération de la clé primaire. **GenerationType.SEQUENCE** signifie que la génération de la clé primaire se fera par une séquence définie dans le SGBD, auquel on ajoute l’attribut generator.

– l'annotation **@Column** est utilisée pour définir la colonne dans la base de données qui mappe le champ annoté.
  
– **CascadeType.ALL** signifie que la persistance propagera (en cascade) toutes les opérations EntityManager (PERSIST, REMOVE, REFRESH, MERGE, DETACH) aux entités associées.

Ici, nous allons utiliser **Lombok** : est une bibliothèque Java qui se connecte automatiquement à un éditeur afin de générer automatiquement les méthodes getter ou equals à l'aide des annotations.
  
* **@Getter / @Setter** :  pour générer automatiquement le getter/setter par défaut.

```java
@Entity
@Getter
@Setter
@Table(name = "songs")
public class Song extends AbstractAuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SONG_SEQ")
    @SequenceGenerator(name = "SONG_SEQ", sequenceName = "song_seq", allocationSize = 1)
    private Long id;

    @Column(name = "title")
    @NotBlank(message = "titre ne doit pas être null ou vide")
    @Size(min = 3, max = 50, message = "titre doit être compris entre 3 et 50 caractères")
    private String title;

    @Column(name = "description")
    @NotBlank(message = "description ne doit pas être nulle ou vide")
    @Size(min = 3, max = 50, message = "description doit être compris entre 3 et 50 caractères")
    private String description;

    @Column(name = "duration")
    @NotBlank(message = "duration ne doit pas être nulle")
    private String duration;

    @Column(name = "artist_name")
    @NotBlank(message = "artistname ne doit pas être null")
    private String artistName;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "categorie<JAZZ, POP, CLASSICAL> ne doit pas être nulle")
    private SongCategory category;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "melody_id", unique = true, nullable = false)
    @NotNull(message = "melody ne doit pas être null")
    private Melody melody;
}
```

* **Melody.java**

L’entité « Melody » est mappée à une table nommée « melody » dans la base de données

– l'annotation **@JsonIgnore**  est utilisé au niveau du champ pour marquer une propriété ou une liste de propriétés à ignorer.

– **orphanRemoval** est spécifique à l'ORM. Il marque que l'entité "enfant" doit être supprimée lorsqu'elle n'est plus référencée à partir de l'entité "parent", par exemple. lorsque vous supprimez l'entité enfant de la collection correspondante de l'entité parent.

```java
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "melody")
public class Melody extends AbstractAuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MELODY_SEQ")
    @SequenceGenerator(name = "MELODY_SEQ", sequenceName = "melody_seq", allocationSize = 1)
    private Long id;

    @Column(name = "pitch")
    @NotBlank(message = "pitch ne doit pas être null ou vide")
    @Size(min = 3, max = 50, message = "titre doit être compris entre 3 et 50 caractères")
    private String pitch;

    @Column(name = "duration")
    @NotBlank(message = "duration ne doit pas être nulle ou vide")
    private String duration;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "type melody<COLOR, BLENDS, DIRECTION> ne doit pas être null")
    private MelodyType type;

    @OneToOne(mappedBy = "melody", orphanRemoval = true)
    @JsonIgnore
    private Song song;
}
```
### III. enums
La classe « **SongCategory** » contient les différentes valeurs possibles d’une catégorie.

```java 
public enum SongCategory {
    JAZZ,
    CLASSICAL,
    POP
}
```

La classe « **MelodyType** » contient les différentes valeurs possibles d’un type de mélodie.

```java 
public enum MelodyType {
    COLOR,
    BLENDS,
    DIRECTION
}
```
## IV. Reposirory
Spring framework nous fournit des repositories afin d’encapsuler des détails de la couche de persistance et de fournir une interface CRUD pour une seule entité ⇒ la possibilité de générer toutes sortes d'opérations vers la base de données.

**Spring Data JPA** est le module qui nous permet d’interagir avec une base de données relationnelles en représentant les objets du domaine métier sous la forme d’entités JPA.

L’annotation **@Repository** est une spécialisation de l’annotation **@Component** ⇒ Pour indiquer que la classe définit un référentiel de données

* **SongRepository.java**

Cette interface est utilisée pour accéder aux chansons de la base de données et qui s'étend de JpaRepository.

Avec **JpaRepository**, nous pouvons :

 * Bénéficier automatiquement des méthodes héritées tels que : **findAll(), findById()** …
 * Utiliser les "query methods" qui utilise une convention de nom pour générer automatiquement le code sous-jacent et exécuter la requête tels que :
   
    – **findSongsByCategory()**: renvoie toutes les chansons ayant une valeur de category en paramètre (JAZZ, POP, CLASSICAL).
   
    – **findSongsByArtistNameContaining()**: renvoie toutes les chansons qui ont le nom de l’artiste en paramètre.
   
    – **findSongByMelody_Id()**: renvoie la chanson qui contient l'id de la mélodie en paramètre.
  
    – **findSongsByMelody_Type()**: renvoie toutes les chansons qui ont le type de la mélodie en paramètre.

```java
@Repository
@EnableJpaAuditing
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findSongsByCategory(SongCategory category);
    List<Song> findSongsByArtistName(String artistName);
    Optional<Song> findSongByMelody_Id(Long id);
    List<Song> findSongsByMelody_Type(MelodyType type);
}
```

## V. Service
* **ISongService**

```java
public interface ISongService {

    List<Song> getAllSongs();

    List<Song> getSongsByCategory(String category);

    List<Song> getSongsByArtistName(String artistName);

    Song getSongsByMelodyId(Long id);

    List<Song> getSongsByMelodyType(String type);

    Song getSongById(Long id);

    Song createSong(Song song);

    Song updateSong(Song song);

    void deleteSongById(Long id);
}
```

* **SongServiceImpl**

L'annotation **@Transactional** peut être utilisée pour indiquer au conteneur les méthodes qui doivent s'exécuter dans un contexte transactionnel.

L’annotation **@Transactional(readOnly = true)** permet d’indiquer si la transaction est en lecture seule (false par défaut) ⇒ Pour les interactions avec les bases de données, les transactions en lecture seule signifient que l’on n’effectue que des requêtes pour lire des données.

```java
@Service
@Transactional
public class SongServiceImpl implements ISongService {
    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

    private final SongRepository songRepository;

    public SongServiceImpl(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByCategory(String category) {
        SongCategory searchedCategory = EnumUtils.getEnumIgnoreCase(SongCategory.class, category);
        if (searchedCategory == null) {
            throw new ResourceNotFoundException("Not found Category with value = " + category);
        }
        return songRepository.findSongsByCategory(searchedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByArtistName(String artistName) {
        return songRepository.findSongsByArtistName(artistName);
    }

    @Override
    @Transactional(readOnly = true)
    public Song getSongsByMelodyId(Long id) {
        return songRepository.findSongByMelody_Id(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with melody id = " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getSongsByMelodyType(String type) {
        MelodyType searchedType = EnumUtils.getEnumIgnoreCase(MelodyType.class, type);
        if (searchedType == null) {
            throw new ResourceNotFoundException("Not found type Melody with value = " + type);
        }
        return songRepository.findSongsByMelody_Type(searchedType);
    }

    @Override
    @Transactional(readOnly = true)
    public Song getSongById(Long id) {
        return songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + id));
    }

    @Override
    public Song createSong(Song song) {
        if(!StringUtils.hasText(song.getMelody().getPitch())){
            throw new FormatNotValidException("Melody : pitch ne doit pas être null ou vide");
        }
        if(!StringUtils.hasText(song.getMelody().getDuration())){
            throw new FormatNotValidException("Melody : duration ne doit pas être null ou vide");
        }
        if(song.getMelody().getType() == null){
            throw new FormatNotValidException("Melody : type ne doit pas être null");
        }
        return songRepository.save(song);
    }

    @Override
    public Song updateSong(Song song) {

        Song searchedSong = songRepository.findById(song.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + song.getId()));

        searchedSong.setTitle(song.getTitle());
        searchedSong.setDescription(song.getDescription());
        searchedSong.setArtistName(song.getArtistName());
        searchedSong.setCategory(song.getCategory());
        searchedSong.setDuration(song.getDuration());

        return songRepository.saveAndFlush(song);
    }

    @Override
    public void deleteSongById(Long id) {
        songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found song with id = " + id));

        songRepository.deleteById(id);
    }
}
```

## VI. Resource
* **SongResource**

Ce contrôleur expose des end-point pour faire les CRUD (créer, récupérer, mettre à jour, supprimer et trouver) des chansons.

##### Points de terminaison d’API

- Les codes de réponse HTTP: 

    * **200 Success** : La demande a réussi
    * **201 Created** : La demande a été satisfaite et a entraîné la création d'une nouvelle ressource
    * **204 No Content** : La demande a répondu à la demande mais n'a pas besoin de retourner un corps d'entité
    * **400 Bad Request** : La requête n'a pas pu être comprise par le serveur en raison d'une syntaxe mal formée
    * **404 Not Found** : Le serveur n'a rien trouvé correspondant à l'URI de la requête

| Méthode HTTP | URI | Description | Codes d'états http |
| ------------- | ------------- | ------------- | ------------- |
| POST  | /api/songs  | Créer une chanson  | 201, 400  |
| PUT  | /api/songs/{id}  | Modifier une chanson  | 200, 404  |
| GET  | /api/songs/{id}  | Récupérer une chanson | 200, 404 |
| GET  | /api/songs  | Récupérer toutes les chansons  | 200  |
| GET  | /api/songs/category/{category} | Récupérer toutes les chansons par catégorie  | 200, 404  |
| GET  | /api/songs/artist/{artistName} | Récupérer toutes les chansons par nom d'artiste  | 200  |
| GET  | /api/songs/melody/id/{id} | Récupérer une chanson par l'id de la mélodie  | 200, 404  |
| GET  | /api/songs/melody/type/{type} | Récupérer toutes les chansons par type de la mélodie  | 200, 404  |
| DELETE  | /api/songs/{id}  | Supprimer une chanson | 204, 404  |

– l'annotation **@RestController** est utilisée pour définir un contrôleur.

⇒ **@RestController** remplace principalement :

**@Controller** : pour dire que c'est un controlleur, pour que spring le charge dans son context, et pour le rendre singleton.

**@ResponseBody** : pour indiquer que la valeur de retour des méthodes doit être liée au corps de la réponse Web.

**@RequestMapping("/api/songs")** déclare que toutes les URL d'Apis dans le contrôleur commenceront par /api/songs.

– Nous avons injecté la classe **ISongService** par constructeur.

```java
@RestController
@RequestMapping("/api/songs")
public class SongResource {

    final private ISongService ISongService;
    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);

    public SongResource(ISongService ISongService) {
        this.ISongService = ISongService;
    }

    @GetMapping
    public ResponseEntity<List<Song>> getAllSongs() {
        List<Song> songs = ISongService.getAllSongs();
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }


    @GetMapping("/category/{category}")
    public ResponseEntity<List<Song>> getSongsByCategory(@PathVariable String category) {
        List<Song> songs = ISongService.getSongsByCategory(category);
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }

    @GetMapping("/artist/{artistName}")
    public ResponseEntity<List<Song>> getSongsByArtist(@PathVariable String artistName) {
        List<Song> songs = ISongService.getSongsByArtistName(artistName);
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }

    @GetMapping("/melody/id/{id}")
    public ResponseEntity<Song> getSongsByMelodyId(@PathVariable Long id) {
        return new ResponseEntity<>(ISongService.getSongsByMelodyId(id), HttpStatus.OK);
    }

    @GetMapping("/melody/type/{type}")
    public ResponseEntity<List<Song>> getSongsByMelodyType(@PathVariable String type) {
        List<Song> songs = ISongService.getSongsByMelodyType(type);
        return new ResponseEntity<>(songs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Song> getSongById(@PathVariable Long id) {
        Song song = ISongService.getSongById(id);
        return new ResponseEntity<>(song, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Song> createSong(@Valid @RequestBody Song song) {
        Song addedSong = ISongService.createSong(song);
        return new ResponseEntity<>(addedSong, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity updateSong(@Valid @RequestBody Song song) {
        return new ResponseEntity<>(ISongService.updateSong(song), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSongById(@PathVariable Long id) {
        ISongService.deleteSongById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
```

## VII. Documentation des API Spring Rest à l'aide de Swagger : Package « config »
Swagger est le framework d'API le plus populaire avec une prise en charge de plus de 40 langues différentes. Nous pouvons utiliser swagger pour concevoir, construire et documenter nos REST API.

```java
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(paths()::test)
                .build();

    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Swagger Playlists APIs")
                .description("This page lists all the rest apis for Playlists App.")
                .version("1.0")
                .build();
    }

    private Predicate<String> paths() {
        return ((Predicate<String>) regex("/error.*")::apply).negate()
                .and(regex("/.*")::apply);
    }
}
```

Utiliser cette url : **http://localhost:8080/swagger-ui.html**

![Screenshot](src/main/resources/images/swagger-ui.png)

## VIII. Exceptions

* **@Builder** : nous permet de produire automatiquement le code requis pour que la classe soit instanciable et aussi pour éviter la complexité des constructeurs

* La classe **ErrorMessage**

```java
@Getter
class ErrorMessage {

    private int statusCode;
    private Date timeStamp;
    private String message;
    private String description;
    private List<FieldError> fieldErrors;

    @Builder
    private ErrorMessage(int statusCode, Date timeStamp, String message, String description, List<FieldError> fieldErrors) {
        this.statusCode = statusCode;
        this.timeStamp = timeStamp;
        this.message = message;
        this.description = description;
        this.fieldErrors = fieldErrors;
    }
}
```

* La classe **FieldError**

```java
/**
 * instead of using default error response provided by Spring Boot,
 * FieldError class is part of ErrorMessage class to definr error response message
 */

@Getter
class FieldError {

    private String objectName;

    private String field;

    private String message;

    @Builder
    private FieldError(String objectName, String field, String message) {
        this.objectName = objectName;
        this.field = field;
        this.message = message;
    }
}
```

* **Gestion des exceptions : créer une exception personnalisée**

Spring prend en charge la gestion des exceptions par :
-	Un gestionnaire d'exceptions global (@ExceptionHandler )
-	Controller Advice (@ControllerAdvice )

L’annotation @ControllerAdvice est la spécialisation de l’annotation @Component afin qu'elle soit détectée automatiquement via l'analyse du chemin de classe. Un Conseil de Contrôleur est une sorte d'intercepteur qui entoure la logique de nos Contrôleurs et nous permet de leur appliquer une logique commune.

Les méthodes (annotées avec @ExceptionHandler) sont partagées globalement entre plusieurs composants @Controller pour capturer les exceptions et les traduire en réponses HTTP.

L’annotation @ExceptionHandler indique quel type d'exception nous voulons gérer. L'instance exception et le request seront injectés via des arguments de méthode.
 
 ⇨	En utilisant deux annotations ensemble, nous pouvons : contrôler le corps de la réponse avec le code d'état et gérer plusieurs exceptions dans la même méthode.

* Nous allons lancer une exception pour la ressource introuvable dans le contrôleur Spring Boot.Créons une classe ResourceNotFoundException qui étend RuntimeException.

```java
/**
 * ResourceNotFoundException class extends RuntimeException.
 * It's about a custom exception :
 * throwing an exception for resource not found in Spring Boot Service
 * ResourceNotFoundException is thrown with Http 404
 */

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

* Nous allons lancer une exception pour une format non valide dans le contrôleur Spring Boot.Créons une classe FormatNotValidException qui étend RuntimeException.

```java
/**
 * FormatNotValidException class extends RuntimeException.
 * It's about a custom exception :
 * throwing an exception for format not valid in Spring Boot Service
 * eg pitch is null
 * FormatNotValidException is thrown with Http 400
 */

public class FormatNotValidException extends RuntimeException {
    public FormatNotValidException(String message) {
        super(message);
    }
}
```

* La classe ServiceExceptionHandler gère trois exceptions spécifiques (ResoureNotFoundException, FormatNotValidException et MethodArgumentNotValidException) et les exceptions globales à un seul endroit.
 
```java
@ControllerAdvice
public class ServiceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SongServiceImpl.class);


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        ErrorMessage message =
                ErrorMessage.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .timeStamp(new Date())
                        .message(e.getMessage())
                        .description(request.getDescription(false))
                        .build();

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorMessage> handleArgumentNotValidException(MethodArgumentNotValidException e, WebRequest request) {

        BindingResult result = e.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors().stream()
                .map(f -> FieldError
                        .builder()
                        .objectName(f.getObjectName())
                        .field(f.getField())
                        .message(f.getCode() + ": " + f.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ErrorMessage message =
                ErrorMessage.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .timeStamp(new Date())
                        .message(e.getMessage())
                        .description(request.getDescription(false))
                        .fieldErrors(fieldErrors)
                        .build();


        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(FormatNotValidException.class)
    public final ResponseEntity<ErrorMessage> handleFormatNotValidException(FormatNotValidException e, WebRequest request) {

        ErrorMessage message = ErrorMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .timeStamp(new Date())
                .message(e.getMessage())
                .description(request.getDescription(false))
                .build();

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> globalException(Exception e, WebRequest request) {
        ErrorMessage message =
                ErrorMessage.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .timeStamp(new Date())
                        .message(e.getMessage())
                        .description(request.getDescription(false))
                        .build();

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

## IX. Tests
Pour les tests unitaires et tests d'intégration, nous allons utiliser une base de données en mémoire H2 comme source de données pour les tests

Dons, nous allons créer un fichier **application.properties** pour les tests sous test/resources

![Screenshot](src/main/resources/images/resource-test.PNG)

```yaml
spring.datasource.url=jdbc:h2:mem:test_playlist_song_melody_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=playlistadmin
spring.datasource.password=admin
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=update
```

##### Tests Unitaires


* **Repository :**

**@DataJpaTest** : Pour tester les référentiels Spring Data JPA, ou tout autre composant lié à JPA, Spring Boot fournit l'annotation **@DataJpaTest**. Nous pouvons simplement l'ajouter à notre test unitaire et il configurera un contexte d'application Spring.

Il désactivera la configuration automatique complète, puis n'appliquera que la configuration d'activation pertinente pour les tests JPA. Par défaut, les tests annotés avec **@DataJpaTest** sont transactionnels et sont annulés à la fin de chaque test.

**@AutoConfigureTestDatabase** : nous devons dire au framework de test Spring qu'il ne devrait pas essayer de remplacer notre base de données. Nous pouvons le faire en utilisant l'annotation 

@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)

**@Before** Pour tester la logique de la base de données, nous avons initialement besoin de données avec lesquelles travailler, nous pouvons le faire en construisant manuellement les objets et en les enregistrant dans la base de données à l'aide de Java dans la section @Before 🡺 Ceci est utile lorsque nous voulons exécuter du code commun avant d'exécuter un test.

##### SongRepositoryTest

```java
@DataJpaTest
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class SongRepositoryTest {

    private static final Logger log = LoggerFactory.getLogger(SongRepositoryTest.class);

    @Autowired
    SongRepository songRepository;
    private Song savedSong;

    @Before
    public void setupCreateSong() {

        Melody melody = new Melody();
        melody.setPitch("Melody Pitch");
        melody.setDuration("03:56");
        melody.setType(MelodyType.COLOR);

        Song song =  new Song();
        song.setTitle("For The Lover That I Lost");
        song.setDescription("Live At Abbey Road Studios");
        song.setCategory(SongCategory.POP);
        song.setArtistName("Sam Smith");
        song.setDuration("3:01");
        song.setMelody(melody);

        savedSong = songRepository.save(song);
        assertThat(savedSong).isNotNull();
        assertThat(savedSong).hasFieldOrPropertyWithValue("title", "For The Lover That I Lost");
        assertThat(savedSong).hasFieldOrPropertyWithValue("description", "Live At Abbey Road Studios");
        assertThat(savedSong).hasFieldOrPropertyWithValue("category", SongCategory.POP);
        assertThat(savedSong).hasFieldOrPropertyWithValue("duration", "3:01");
        assertThat(savedSong).hasFieldOrPropertyWithValue("artistName", "Sam Smith");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.pitch", "Melody Pitch");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.duration", "03:56");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.type", MelodyType.COLOR);
    }

    @Test
    public void shouldFindAllSongs() {
        List<Song> songs = songRepository.findAll();
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(songs.get(songs.size() - 1));
        assertThat(songs.get(songs.size() - 1).getId()).isNotNull();
    }

    @Test
    public void shouldFindSongsByCategory() {
        List<Song> songs = songRepository.findSongsByCategory(savedSong.getCategory());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongsByArtistName() {
        List<Song> songs = songRepository.findSongsByArtistName(savedSong.getArtistName());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongsByMelodyId() {
        Song song = songRepository.findSongByMelody_Id(savedSong.getMelody().getId()).orElse(null);
        assertThat(song).isNotNull();
        assertThat(song).isEqualTo(savedSong);
        assertThat(song).hasFieldOrPropertyWithValue("title", savedSong.getTitle());
        assertThat(song).hasFieldOrPropertyWithValue("description", savedSong.getDescription());
        assertThat(song).hasFieldOrPropertyWithValue("category", savedSong.getCategory());
        assertThat(song).hasFieldOrPropertyWithValue("duration", savedSong.getDuration());
        assertThat(song).hasFieldOrPropertyWithValue("artistName", savedSong.getArtistName());
        assertThat(song).hasFieldOrPropertyWithValue("melody.pitch", savedSong.getMelody().getPitch());
        assertThat(song).hasFieldOrPropertyWithValue("melody.duration", savedSong.getMelody().getDuration());
        assertThat(song).hasFieldOrPropertyWithValue("melody.type", savedSong.getMelody().getType());
    }

    @Test
    public void shouldFindSongsByMelodyType() {
        List<Song> songs = songRepository.findSongsByMelody_Type(savedSong.getMelody().getType());
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        assertThat(songs).contains(savedSong);
    }

    @Test
    public void shouldFindSongById() {
        Song foundSong = songRepository.findById(savedSong.getId()).orElse(null);
        assertThat(foundSong).isNotNull();
        assertThat(foundSong).isEqualTo(savedSong);
        assertThat(foundSong).hasFieldOrPropertyWithValue("title", savedSong.getTitle());
        assertThat(foundSong).hasFieldOrPropertyWithValue("description", savedSong.getDescription());
        assertThat(foundSong).hasFieldOrPropertyWithValue("category", savedSong.getCategory());
        assertThat(foundSong).hasFieldOrPropertyWithValue("duration", savedSong.getDuration());
        assertThat(foundSong).hasFieldOrPropertyWithValue("artistName", savedSong.getArtistName());
        assertThat(foundSong).hasFieldOrPropertyWithValue("melody.pitch", savedSong.getMelody().getPitch());
        assertThat(foundSong).hasFieldOrPropertyWithValue("melody.duration", savedSong.getMelody().getDuration());
        assertThat(foundSong).hasFieldOrPropertyWithValue("melody.type", savedSong.getMelody().getType());
    }

    @Test
    public void shoulCreateSong() {

        int sizeBeforeCreate = songRepository.findAll().size();

        Melody melody = new Melody();
        melody.setPitch("Melody Pitch test");
        melody.setDuration("07:56");
        melody.setType(MelodyType.DIRECTION);

        Song songToSave = new Song();
        songToSave.setTitle("The Falls");
        songToSave.setDescription("Album musical d'Ennio Morricone");
        songToSave.setCategory(SongCategory.CLASSICAL);
        songToSave.setArtistName("Morricone");
        songToSave.setDuration("7:10");
        songToSave.setMelody(melody);

        Song song = songRepository.save(songToSave);

        int sizeAfterCreate = songRepository.findAll().size();
        assertThat(sizeAfterCreate).isEqualTo(sizeBeforeCreate + 1);
        assertThat(song).isNotNull();
        assertThat(song).hasFieldOrPropertyWithValue("title", "The Falls");
        assertThat(song).hasFieldOrPropertyWithValue("description", "Album musical d'Ennio Morricone");
        assertThat(song).hasFieldOrPropertyWithValue("category", SongCategory.CLASSICAL);
        assertThat(song).hasFieldOrPropertyWithValue("duration", "7:10");
        assertThat(song).hasFieldOrPropertyWithValue("artistName", "Morricone");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.pitch", "Melody Pitch");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.duration", "03:56");
        assertThat(savedSong).hasFieldOrPropertyWithValue("melody.type", MelodyType.COLOR);

    }

    @Test
    public void shouldUpdateSong() {

        Song foundSong = songRepository.getById(savedSong.getId());
        assertThat(foundSong).isNotNull();

        foundSong.setTitle("Power");
        foundSong.setDescription("power album");
        foundSong.setArtistName("Isak Danielson");
        foundSong.getMelody().setType(MelodyType.BLENDS);
        Song updatedSong = songRepository.save(foundSong);

        Song checkSong = songRepository.getById(updatedSong.getId());

        assertThat(checkSong.getId()).isNotNull();
        assertThat(checkSong.getId()).isEqualTo(updatedSong.getId());
        assertThat(checkSong.getTitle()).isEqualTo(updatedSong.getTitle());
        assertThat(checkSong.getDescription()).isEqualTo(updatedSong.getDescription());
        assertThat(checkSong.getCategory()).isEqualTo(updatedSong.getCategory());
        assertThat(checkSong.getDuration()).isEqualTo(updatedSong.getDuration());
        assertThat(checkSong.getArtistName()).isEqualTo(updatedSong.getArtistName());
    }

    @Test
    public void shouldDeleteSonById() {
        int sizeBeforeDelete = songRepository.findAll().size();

        Song foundSong = songRepository.getById(savedSong.getId());
        assertThat(foundSong).isNotNull();

        songRepository.deleteById(foundSong.getId());

        int sizeAfterDelete = songRepository.findAll().size();

        assertThat(sizeAfterDelete).isEqualTo(sizeBeforeDelete - 1);
    }
}
```

* **Service**

 **SongServiceUnitTest**
```java
@RunWith(SpringRunner.class)
public class SongServiceUnitTest {

    private final static Logger log = LoggerFactory.getLogger(SongServiceUnitTest.class);

    @MockBean
    private SongRepository songRepository;

    private SongServiceImpl songService;

    private Song mySong;
    private List<Song> songList = new ArrayList<>();


    @Before
    public void setup() {
        songService = new SongServiceImpl(songRepository);

        Melody myMelody = new Melody();
        myMelody.setPitch("Melody Pitch");
        myMelody.setDuration("03:56");
        myMelody.setType(MelodyType.COLOR);

        mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
        mySong.setMelody(myMelody);
    }

    @Test
    public void testGetAllSongs() {
        songRepository.save(mySong);
        when(songRepository.findAll()).thenReturn(songList);

        //test
        List<Song> songs = songService.getAllSongs();
        assertEquals(songs, songList);
        verify(songRepository, times(1)).save(mySong);
        verify(songRepository, times(1)).findAll();
    }

    @Test
    public void testGetSongsByCategory() {
        songList.add(mySong);
        when(songRepository.findSongsByCategory(SongCategory.POP)).thenReturn(songList);

        //test
        List<Song> songs = songService.getSongsByCategory("POP");
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByCategory(SongCategory.POP);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistCategory() {
        List<Song> songs = songService.getSongsByCategory("Popy");
        assertTrue(songs.isEmpty());
    }

    @Test
    public void testGetSongsByArtistName() {
        songList.add(mySong);
        when(songRepository.findSongsByArtistName(mySong.getArtistName())).thenReturn(songList);
        List<Song> songs = songService.getSongsByArtistName(mySong.getArtistName());

        //test
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByArtistName(mySong.getArtistName());
    }

    @Test
    public void testGetSongsByMelodyId() {
        when(songRepository.findSongByMelody_Id(mySong.getMelody().getId())).thenReturn(Optional.of(mySong));
        Song song = songService.getSongsByMelodyId(mySong.getMelody().getId());

        //test
        assertThat(song).isNotNull();
        assertThat(song).isEqualTo(mySong);
        assertThat(song.getId()).isNotNull();
        assertThat(song.getId()).isEqualTo(mySong.getId());
        assertThat(song.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(song.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(song.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(song.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(song.getArtistName()).isEqualTo(mySong.getArtistName());
        assertThat(song.getMelody().getPitch()).isEqualTo(mySong.getMelody().getPitch());
        assertThat(song.getMelody().getDuration()).isEqualTo(mySong.getMelody().getDuration());
        assertThat(song.getMelody().getType()).isEqualTo(mySong.getMelody().getType());
        verify(songRepository, times(1)).findSongByMelody_Id(mySong.getMelody().getId());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingMelodyId() {
        when(songRepository.findSongByMelody_Id(2000L)).thenReturn(Optional.empty());
        songService.getSongsByMelodyId(2000L);
    }

    @Test
    public void testGetSongsByMelodyType() {
        songList.add(mySong);
        when(songRepository.findSongsByMelody_Type(MelodyType.COLOR)).thenReturn(songList);

        //test
        List<Song> songs = songService.getSongsByMelodyType("COLOR");
        assertThat(songs).isNotEmpty();
        assertThat(songs).hasSizeGreaterThanOrEqualTo(1);
        verify(songRepository, times(1)).findSongsByMelody_Type(MelodyType.COLOR);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistMelodyType() {
        List<Song> songs = songService.getSongsByMelodyType("COLORRRR");
        assertTrue(songs.isEmpty());
    }


    @Test
    public void testCreateSong() {
        when(songRepository.save(any(Song.class))).thenReturn(mySong);
        songService.createSong(mySong);
        verify(songRepository, times(1)).save(any(Song.class));
    }

    @Test
    public void testUpdateSong() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.of(mySong));

        mySong.setTitle("Power");
        mySong.setDescription("power album");
        mySong.setArtistName("Isak Danielson");

        given(songRepository.saveAndFlush(mySong)).willReturn(mySong);

        Song updatedSong = songService.updateSong(mySong);

        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong).isEqualTo(mySong);
        assertThat(updatedSong.getId()).isNotNull();
        assertThat(updatedSong.getId()).isEqualTo(mySong.getId());
        assertThat(updatedSong.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(updatedSong.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(updatedSong.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(updatedSong.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(updatedSong.getArtistName()).isEqualTo(mySong.getArtistName());
        assertThat(updatedSong.getMelody().getPitch()).isEqualTo(mySong.getMelody().getPitch());
        assertThat(updatedSong.getMelody().getDuration()).isEqualTo(mySong.getMelody().getDuration());
        assertThat(updatedSong.getMelody().getType()).isEqualTo(mySong.getMelody().getType());
    }


    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateSongWithNonExistingId() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.empty());
        songService.updateSong(mySong);

    }

    @Test
    public void testGetSongsById() {
        // when
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.ofNullable(mySong));
        Song foundSong = songService.getSongById(mySong.getId());

        //test - then
        assertThat(foundSong).isNotNull();
        assertThat(foundSong).isEqualTo(mySong);
        assertThat(foundSong.getId()).isNotNull();
        assertThat(foundSong.getId()).isEqualTo(1000L);
        assertThat(foundSong.getId()).isEqualTo(mySong.getId());
        assertThat(foundSong.getTitle()).isEqualTo(mySong.getTitle());
        assertThat(foundSong.getDescription()).isEqualTo(mySong.getDescription());
        assertThat(foundSong.getCategory()).isEqualTo(mySong.getCategory());
        assertThat(foundSong.getDuration()).isEqualTo(mySong.getDuration());
        assertThat(foundSong.getArtistName()).isEqualTo(mySong.getArtistName());
        assertThat(foundSong.getMelody().getPitch()).isEqualTo(mySong.getMelody().getPitch());
        assertThat(foundSong.getMelody().getDuration()).isEqualTo(mySong.getMelody().getDuration());
        assertThat(foundSong.getMelody().getType()).isEqualTo(mySong.getMelody().getType());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingId() {

        // when
        when(songRepository.findById(4000L)).thenReturn(Optional.empty());
        songService.getSongById(4000L);
    }

    @Test
    public void testGetSongsWithNonExistingIdV2() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> songService.getSongById(4000L));

        assertThat(ex.getMessage()).isEqualTo("Not found song with id = 4000");
    }

    @Test
    public void testDeleteSongById() {
        when(songRepository.findById(mySong.getId())).thenReturn(Optional.of(mySong));
        songService.deleteSongById(mySong.getId());
        verify(songRepository, times(1)).deleteById(mySong.getId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeleteSongWithNonExistingId() {
        when(songRepository.findById(4000L)).thenReturn(Optional.empty());
        songService.deleteSongById(4000L);
    }
}
``` 

* **Resource**

 **SongResourceUnitTest**
    
```java
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SongResource.class)
public class SongResourceUnitTest {

    private static final Logger log = LoggerFactory.getLogger(SongResourceUnitTest.class);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ISongService songService;

    private Song mySong;
    private List<Song> songList = new ArrayList<>();


    @Before
    public void setup() {
        Melody melody = new Melody();
        melody.setId(2000L);
        melody.setPitch("Melody Pitch");
        melody.setDuration("03:56");
        melody.setType(MelodyType.COLOR);


        mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
        mySong.setMelody(melody);
    }

    @Test
    public void testGetAllSongs() throws Exception {
        songList.add(mySong);
        when(songService.getAllSongs()).thenReturn(songList);

        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
        verify(songService).getAllSongs();
        verify(songService, times(1)).getAllSongs();
    }

    @Test
    public void testGetEmptyListSongs() throws Exception {
        when(songService.getAllSongs()).thenReturn(songList);

        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetSongsByCategory() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByCategory("POP")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/category/" + mySong.getCategory())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
    }

    @Test
    public void testGetEmptyListSongsByCategory() throws Exception {
        when(songService.getSongsByCategory("CLASSICAL")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/category/CLASSICAL")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    public void testGetSongsWithNonExistingCategory() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Category with value = popy")).when(songService).getSongsByCategory("popy");
        mockMvc.perform(get("/api/songs/category/popy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Category with value = popy"));
    }


    @Test
    public void testGetSongsByArtistName() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByArtistName(mySong.getArtistName())).thenReturn(songList);

        mockMvc.perform(get("/api/songs/artist/" + mySong.getArtistName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
    }

    @Test
    public void testGetEmptyListSongsByArtistName() throws Exception {
        when(songService.getSongsByArtistName("Isak")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/artist/Isak")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetSongsByMelodyId() throws Exception {
        when(songService.getSongsByMelodyId(mySong.getMelody().getId())).thenReturn(mySong);

        mockMvc.perform(get("/api/songs/melody/id/" + mySong.getMelody().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(mySong.getTitle()))
                .andExpect(jsonPath("$.description").value(mySong.getDescription()))
                .andExpect(jsonPath("$.category").value(mySong.getCategory().toString()))
                .andExpect(jsonPath("$.artistName").value(mySong.getArtistName()))
                .andExpect(jsonPath("$.duration").value(mySong.getDuration()))
                .andExpect(jsonPath("$.melody.pitch").value(mySong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(mySong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.pitch").value(mySong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(mySong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.type").value(mySong.getMelody().getType().toString()));
    }


    @Test
    public void testGetSongByNonExistingMelodyId() throws Exception {
        doThrow(new ResourceNotFoundException("Not found song with melody id = 2000")).when(songService).getSongsByMelodyId(2000L);
        mockMvc.perform(get("/api/songs/melody/id/2000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found song with melody id = 2000"));
    }


    @Test
    public void testGetSongsByMelodyType() throws Exception {
        songList.add(mySong);
        when(songService.getSongsByMelodyType("COLOR")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/melody/type/" + mySong.getMelody().getType())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].title").value(songList.get(0).getTitle()))
                .andExpect(jsonPath("$[*].description").value(songList.get(0).getDescription()))
                .andExpect(jsonPath("$[*].category").value(songList.get(0).getCategory().toString()))
                .andExpect(jsonPath("$[*].artistName").value(songList.get(0).getArtistName()))
                .andExpect(jsonPath("$[*].duration").value(songList.get(0).getDuration()))
                .andExpect(jsonPath("$[*].melody.pitch").value(songList.get(0).getMelody().getPitch()))
                .andExpect(jsonPath("$[*].melody.duration").value(songList.get(0).getMelody().getDuration()))
                .andExpect(jsonPath("$[*].melody.type").value(songList.get(0).getMelody().getType().toString()));
    }

    @Test
    public void testGetEmptyListSongsByMelodyType() throws Exception {
        when(songService.getSongsByMelodyType("DIRECTION")).thenReturn(songList);

        mockMvc.perform(get("/api/songs/melody/type/DIRECTION")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    public void testGetSongsWithNonExistingMelodyType() throws Exception {
        doThrow(new ResourceNotFoundException("Not found type Melody with value = DIRECTyyy")).when(songService).getSongsByMelodyType("DIRECTyyy");
        mockMvc.perform(get("/api/songs/melody/type/DIRECTyyy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found type Melody with value = DIRECTyyy"));
    }


    @Test
    public void testGetSongById() throws Exception {
        mySong.setId(1000L);
        when(songService.getSongById(mySong.getId())).thenReturn(mySong);

        mockMvc.perform(get("/api/songs/" + mySong.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(mySong.getTitle()))
                .andExpect(jsonPath("$.description").value(mySong.getDescription()))
                .andExpect(jsonPath("$.category").value(mySong.getCategory().toString()))
                .andExpect(jsonPath("$.artistName").value(mySong.getArtistName()))
                .andExpect(jsonPath("$.duration").value(mySong.getDuration()))
                .andExpect(jsonPath("$.melody.pitch").value(mySong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(mySong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.type").value(mySong.getMelody().getType().toString()));
    }


    @Test
    public void testGetSongByNonExistingId() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Song with id = 1000")).when(songService).getSongById(1000L);
        mockMvc.perform(get("/api/songs/1000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Song with id = 1000"));
    }


    @Test
    public void testCreateSong() throws Exception {
        when(songService.createSong(any(Song.class))).thenReturn(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().isCreated());
        verify(songService, times(1)).createSong(any());
    }

    @Test
    public void testCreateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithTitleNull() throws Exception {
        mySong.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(songService).createSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testUpdateSong() throws Exception {
        mySong.setId(1000L);
        when(songService.updateSong(mySong)).thenReturn(mySong);
        mockMvc.perform(put("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setId(1000L);
        mySong.setTitle("S");
        doThrow(new ResourceNotFoundException("Size: titre doit être compris entre 3 et 50 caractères"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setId(1000L);
        mySong.setDescription("S");
        doThrow(new ResourceNotFoundException("Size: description doit être compris entre 3 et 50 caractères"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithTitleNull() throws Exception {
        mySong.setId(1000L);
        mySong.setTitle(null);
        doThrow(new ResourceNotFoundException("NotBlank: titre ne doit pas être null ou vide"))
                .when(songService).updateSong(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }

    @Test
    public void testDeleteSongById() throws Exception {
        mySong.setId(1000L);
        doNothing().when(songService).deleteSongById(mySong.getId());
        mockMvc.perform(delete("/api/songs/" + mySong.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteNotFoundSong() throws Exception {
        doThrow(new ResourceNotFoundException("Not found Song with id = 1000")).when(songService).deleteSongById(1000L);
        mockMvc.perform(delete("/api/songs/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Song with id = 1000"));
    }
}
``` 

##### Tests d'intégration
    
**@SpringBootTest** :

-	Est une annotation fournie par Spring Boot

-	Elle permet lors de l’exécution des tests d’initialiser le contexte Spring.

-	Les beans de notre application peuvent alors être utilisés

-	Rappelons qu’un test s’exécute de façon unitaire, presque comme une application à part entière. Par défaut, notre test n’a donc aucune connaissance du contexte Spring. Dans le cas d’une application Spring Boot, c’est un vrai problème !

🡺 Le problème est résolu grâce à l’annotation @SpringBootTest.

* **Service**

   **SongServiceIntegrationTest**
    
```java
@SpringBootTest
@Transactional
@RunWith(SpringRunner.class)
public class SongServiceIntegrationTest {

    private final static Logger log = LoggerFactory.getLogger(SongServiceIntegrationTest.class);

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongServiceImpl songService;

    private Song defaultSong;

    @Before
    public void setup() {

        Melody melody = new Melody();
        melody.setPitch("Melody Pitch");
        melody.setDuration("03:56");
        melody.setType(MelodyType.COLOR);

        Song mySong = new Song();
        mySong.setId(1000L);
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
        mySong.setMelody(melody);

        defaultSong = songRepository.saveAndFlush(mySong);

    }

    @Test
    public void testGetAllSongs() {
        List<Song> songs = songService.getAllSongs();
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetSongsByCategory() {
        List<Song> songs = songService.getSongsByCategory("POP");
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingCategory() {
        songService.getSongsByCategory("Popy");
    }

    @Test
    public void testGetSongsWithNonExistingCategoryV2() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> songService.getSongById(4000L));

        assertThat(ex.getMessage()).isEqualTo("Not found song with id = 4000");
    }

    @Test
    public void testGetSongsByArtistName() {
        List<Song> songs = songService.getSongsByArtistName("Sam Smith");
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test
    public void testGetSongsByMelodyId() {
        Song song = songService.getSongsByMelodyId(defaultSong.getMelody().getId());
        assertThat(song).isNotNull();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongWithNonExistingMelodyId() {
        songService.getSongsByMelodyId(2000L);
    }

    @Test
    public void testGetSongsByMelodyType() {
        List<Song> songs = songService.getSongsByMelodyType("COLOR");
        assertThat(songs).isNotNull().isNotEmpty();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongsWithNonExistingMelodyType() {
        songService.getSongsByMelodyType("COLORRS");
    }

    @Test
    public void testGetSongById() {
        Song song = songService.getSongById(defaultSong.getId());
        assertThat(song).isNotNull();
        assertThat(song.getId()).isNotNull();
        assertThat(song.getId()).isEqualTo(defaultSong.getId());
        assertThat(song.getTitle()).isEqualTo(defaultSong.getTitle());
        assertThat(song.getDescription()).isEqualTo(defaultSong.getDescription());
        assertThat(song.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(song.getArtistName()).isEqualTo(defaultSong.getArtistName());
        assertThat(song.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(song.getMelody().getPitch()).isEqualTo(defaultSong.getMelody().getPitch());
        assertThat(song.getMelody().getDuration()).isEqualTo(defaultSong.getMelody().getDuration());
        assertThat(song.getMelody().getType()).isEqualTo(defaultSong.getMelody().getType());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSongWithNonExistingId() {
        songService.getSongById(4000L);
    }

    @Test
    public void testCreateSong() {
        Song savedSong = songService.createSong(defaultSong);
        assertThat(savedSong).isNotNull();
        assertThat(savedSong.getId()).isNotNull();
        assertThat(savedSong.getTitle()).isEqualTo(defaultSong.getTitle());
        assertThat(savedSong.getDescription()).isEqualTo(defaultSong.getDescription());
        assertThat(savedSong.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(savedSong.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(savedSong.getArtistName()).isEqualTo(defaultSong.getArtistName());
        assertThat(savedSong.getMelody().getPitch()).isEqualTo(defaultSong.getMelody().getPitch());
        assertThat(savedSong.getMelody().getDuration()).isEqualTo(defaultSong.getMelody().getDuration());
        assertThat(savedSong.getMelody().getType()).isEqualTo(defaultSong.getMelody().getType());
    }

    @Test
    public void testUpdateSong() {
        defaultSong.setTitle("Broken");
        defaultSong.setDescription("Isak Album");
        defaultSong.setArtistName("Isak Danielson");
        defaultSong.getMelody().setType(MelodyType.BLENDS);

        Song updatedSong = songService.updateSong(defaultSong);
        assertThat(updatedSong).isNotNull();
        assertThat(updatedSong.getId()).isNotNull();
        assertThat(updatedSong.getTitle()).isEqualTo("Broken");
        assertThat(updatedSong.getDescription()).isEqualTo("Isak Album");
        assertThat(updatedSong.getCategory()).isEqualTo(defaultSong.getCategory());
        assertThat(updatedSong.getDuration()).isEqualTo(defaultSong.getDuration());
        assertThat(updatedSong.getArtistName()).isEqualTo("Isak Danielson");
        assertThat(updatedSong.getMelody().getPitch()).isEqualTo("Melody Pitch");
        assertThat(updatedSong.getMelody().getDuration()).isEqualTo("03:56");
        assertThat(updatedSong.getMelody().getType()).isEqualTo(defaultSong.getMelody().getType());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateSongWithNonExistingId() {
        defaultSong.setId(4000L);
        songService.updateSong(defaultSong);

    }

    @Test
    public void testDeleteSongById() {
        songService.deleteSongById(defaultSong.getId());
        Optional<Song> deletedSong = songRepository.findById(defaultSong.getId());
        assertThat(deletedSong.isPresent()).isFalse();

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testDeleteSongWithNonExistingId() {
        songService.deleteSongById(4000L);

    }
}
```   

* **Resource**

Depuis la version 3.2 Spring introduit le framework de test MVC (MockMvc).
Nous avons mis en place le MockMvc. Le MockMvcBuilders.standaloneSetup() permet d'enregistrer un ou plusieurs contrôleurs sans avoir besoin d'utiliser le fichier WebApplicationContext.

La méthode perform permet d’envoyer la requête au serveur Rest. La méthode jsonPath permet d’accéder au contenu de la réponse json.

   **SongResourceIntegrationTest**

```java
@SpringBootTest
@RunWith(SpringRunner.class)
public class SongResourceIntegrationTest {
    private final static Logger log = LoggerFactory.getLogger(SongResourceIntegrationTest.class);

    private MockMvc mockMvc;

    @Autowired
    private ServiceExceptionHandler serviceExceptionHandler;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private ISongService songService;

    private Song mySong;

    @Before
    public void setup() {

        SongResource songResource = new SongResource(songService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(songResource)
                .setControllerAdvice(serviceExceptionHandler)
                .build();


        Melody melody = new Melody();
        melody.setPitch("Melody Pitch");
        melody.setDuration("03:56");
        melody.setType(MelodyType.COLOR);

        mySong =  new Song();
        mySong.setTitle("For The Lover That I Lost");
        mySong.setDescription("Live At Abbey Road Studios");
        mySong.setCategory(SongCategory.POP);
        mySong.setArtistName("Sam Smith");
        mySong.setDuration("3:01");
        mySong.setMelody(melody);

    }

    @Test
    public void testGetAllSongs() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedSong.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedSong.getDescription())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(savedSong.getCategory().toString())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(savedSong.getDuration())))
                .andExpect(jsonPath("$.[*].artistName").value(hasItem(savedSong.getArtistName())))
                .andExpect(jsonPath("$.[*].melody.pitch").value(hasItem(savedSong.getMelody().getPitch())))
                .andExpect(jsonPath("$.[*].melody.duration").value(hasItem(savedSong.getMelody().getDuration())))
                .andExpect(jsonPath("$.[*].melody.type").value(hasItem(savedSong.getMelody().getType().toString())));
    }

    @Test
    public void testGetEmptyListSongs() throws Exception {
        songRepository.deleteAll();
        mockMvc.perform(get("/api/songs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetSongsByCategory() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs/category/{category}", savedSong.getCategory().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedSong.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedSong.getDescription())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(savedSong.getCategory().toString())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(savedSong.getDuration())))
                .andExpect(jsonPath("$.[*].artistName").value(hasItem(savedSong.getArtistName())))
                .andExpect(jsonPath("$.[*].melody.pitch").value(hasItem(savedSong.getMelody().getPitch())))
                .andExpect(jsonPath("$.[*].melody.duration").value(hasItem(savedSong.getMelody().getDuration())))
                .andExpect(jsonPath("$.[*].melody.type").value(hasItem(savedSong.getMelody().getType().toString())));
    }

    @Test
    public void testGetSongsWithNonExistingCategory() throws Exception {
        mockMvc.perform(get("/api/songs/category/popy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found Category with value = popy"));
    }


    @Test
    public void testGetSongsByArtistName() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);

        mockMvc.perform(get("/api/songs/artist/{artist}", savedSong.getArtistName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedSong.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedSong.getDescription())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(savedSong.getCategory().toString())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(savedSong.getDuration())))
                .andExpect(jsonPath("$.[*].artistName").value(hasItem(savedSong.getArtistName())))
                .andExpect(jsonPath("$.[*].melody.pitch").value(hasItem(savedSong.getMelody().getPitch())))
                .andExpect(jsonPath("$.[*].melody.duration").value(hasItem(savedSong.getMelody().getDuration())))
                .andExpect(jsonPath("$.[*].melody.type").value(hasItem(savedSong.getMelody().getType().toString())));
    }


    @Test
    public void testGetSongsByMelodyId() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs/melody/id/{id}", savedSong.getMelody().getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(savedSong.getTitle()))
                .andExpect(jsonPath("$.description").value(savedSong.getDescription()))
                .andExpect(jsonPath("$.category").value(savedSong.getCategory().toString()))
                .andExpect(jsonPath("$.duration").value(savedSong.getDuration()))
                .andExpect(jsonPath("$.artistName").value(savedSong.getArtistName()))
                .andExpect(jsonPath(".melody.pitch").value(savedSong.getMelody().getPitch()))
                .andExpect(jsonPath(".melody.duration").value(savedSong.getMelody().getDuration()))
                .andExpect(jsonPath(".melody.type").value(savedSong.getMelody().getType().toString()));
    }

    @Test
    public void testGetSongByNonExistingMelodyId() throws Exception {
        mockMvc.perform(get("/api/songs/melody/id/4000"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testGetSongsByMelodyType() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs/melody/type/{type}", savedSong.getMelody().getType().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].title").value(hasItem(savedSong.getTitle())))
                .andExpect(jsonPath("$.[*].description").value(hasItem(savedSong.getDescription())))
                .andExpect(jsonPath("$.[*].category").value(hasItem(savedSong.getCategory().toString())))
                .andExpect(jsonPath("$.[*].duration").value(hasItem(savedSong.getDuration())))
                .andExpect(jsonPath("$.[*].artistName").value(hasItem(savedSong.getArtistName())))
                .andExpect(jsonPath("$[*].melody.pitch").value(hasItem(savedSong.getMelody().getPitch())))
                .andExpect(jsonPath("$[*].melody.duration").value(hasItem(savedSong.getMelody().getDuration())))
                .andExpect(jsonPath("$[*].melody.type").value(hasItem(savedSong.getMelody().getType().toString())));
    }

    @Test
    public void testGetSongsWithNonExistingMelodyType() throws Exception {
        mockMvc.perform(get("/api/songs/melody/type/DIRECTyyy")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found type Melody with value = DIRECTyyy"));
    }


    @Test
    public void testGetSongById() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(get("/api/songs/{id}", savedSong.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedSong.getId()))
                .andExpect(jsonPath("$.title").value(savedSong.getTitle()))
                .andExpect(jsonPath("$.description").value(savedSong.getDescription()))
                .andExpect(jsonPath("$.category").value(savedSong.getCategory().toString()))
                .andExpect(jsonPath("$.duration").value(savedSong.getDuration()))
                .andExpect(jsonPath("$.artistName").value(savedSong.getArtistName()))
                .andExpect(jsonPath("$.melody.pitch").value(savedSong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(savedSong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.type").value(savedSong.getMelody().getType().toString()));
    }


    @Test
    public void testGetSongByNonExistingId() throws Exception {
        mockMvc.perform(get("/api/songs/4000"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testCreateSong() throws Exception {
        int sizeBefore = songRepository.findAll().size();
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(savedSong.getTitle()))
                .andExpect(jsonPath("$.description").value(savedSong.getDescription()))
                .andExpect(jsonPath("$.category").value(savedSong.getCategory().toString()))
                .andExpect(jsonPath("$.duration").value(savedSong.getDuration()))
                .andExpect(jsonPath("$.artistName").value(savedSong.getArtistName()))
                .andExpect(jsonPath("$.melody.pitch").value(savedSong.getMelody().getPitch()))
                .andExpect(jsonPath("$.melody.duration").value(savedSong.getMelody().getDuration()))
                .andExpect(jsonPath("$.melody.type").value(savedSong.getMelody().getType().toString()));
    }



    @Test
    public void testCreateSongWithTitleSizeLessThanThree() throws Exception {
        mySong.setTitle("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithDescriptionSizeLessThanThree() throws Exception {
        mySong.setDescription("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testCreateSongWithTitleNull() throws Exception {
        mySong.setTitle(null);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(mySong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testUpdateSong() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setTitle("Song updated");
        mockMvc.perform(put("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateSongWithTitleSizeLessThanThree() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setTitle("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: titre doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithDescriptionSizeLessThanThree() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setDescription("S");
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("Size: description doit être compris entre 3 et 50 caractères"));
    }

    @Test
    public void testUpdateSongWithTitleNull() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        savedSong.setTitle(null);
        mockMvc.perform(post("/api/songs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(asJsonString(savedSong)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("fieldErrors[0].message")
                        .value("NotBlank: titre ne doit pas être null ou vide"));
    }


    @Test
    public void testDeleteSongById() throws Exception {
        Song savedSong = songRepository.saveAndFlush(mySong);
        mockMvc.perform(delete("/api/songs/{id}", savedSong.getId()))
                .andExpect(status().isNoContent());

    }

    @Test
    public void testDeleteNotFoundSong() throws Exception {
        mockMvc.perform(delete("/api/songs/1000"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("message").value("Not found song with id = 1000"));
    }
}
``` 

La classe **TestUtils** contient une méthode qui sert à convertir un objet Json en une chaîne de caractère.

```java

public class TestUtils {
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```
