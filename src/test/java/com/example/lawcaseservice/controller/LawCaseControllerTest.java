package com.example.lawcaseservice.controller;

import com.example.lawcaseservice.domain.LawCase;
import com.example.lawcaseservice.repository.InMemoryRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LawCaseControllerTest {


    @Autowired
    InMemoryRepository repository;

    @Autowired
    LawCaseController lawCaseController;

    @LocalServerPort
    private Integer port;

    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>(
            "mysql:9.0.1"
    );

    @BeforeAll
    static void beforeAll() {
        mySQLContainer.start();
    }

    @AfterAll
    static void afterAll() {
        mySQLContainer .stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
    }

    @BeforeEach
    void setup(){
        RestAssured.baseURI = "http://localhost:" + port;
    }


    private final String API = "/api/lawcase/";

    LawCase first = LawCase.builder()
            .id(1)
            .name("test1")
            .build();

    LawCase second = LawCase.builder()
            .id(2)
            .name("test2")
            .build();

    @Test
    void shouldSaveOneLawCase() {

        repository.deleteAll();

        String requestBody = "{\n" +
                "  \"id\": \"1\",\n" +
                "  \"name\": \"test1\"}";

        Response response = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .post(API)
                .then()
                .extract().response();

        System.out.println(response.jsonPath().prettyPrint());

        Assertions.assertEquals(201, response.statusCode());
        Assertions.assertEquals("1", response.jsonPath().getString("id"));
        Assertions.assertEquals("test1", response.jsonPath().getString("name"));
    }


    @Test
    void updateOneLawCase(){
        repository.deleteAll();

        repository.addLawCase(first);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[test1]", response.jsonPath().getString("name"));


        String id = first.getId().toString();

        String requestBody = "{\n" +
                "  \"id\": \"" + id + "\",\n" +
                "  \"name\": \"Put\"\n}";

        System.out.println(requestBody.toString());

        Response response1 = given()
                .contentType(ContentType.JSON)
                .and()
                .body(requestBody)
                .when()
                .put(API + id)
                .then()
                .extract().response();


        System.out.println(response1.jsonPath().prettyPrint());

        Assertions.assertEquals(200, response1.statusCode());
        Assertions.assertEquals("Put", response1.jsonPath().getString("name"));

        Response response2 = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response2.statusCode());
        Assertions.assertEquals("[Put]", response2.jsonPath().getString("name"));
    }

    @Test
    void shouldFindOneLawCase(){
        repository.deleteAll();
        LawCase saved = repository.addLawCase(first);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API + saved.getId())
                .then()
                .extract().response();

        System.out.println(response.jsonPath().prettyPrint());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("test1", response.jsonPath().getString("name"));
    }


    @Test
    void NotFoundLawCase(){
        repository.deleteAll();
        LawCase saved = repository.addLawCase(first);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API + (saved.getId() +10))
                .then()
                .extract().response();

        System.out.println(response.jsonPath().prettyPrint());

        Assertions.assertEquals(404, response.statusCode());
        Assertions.assertEquals("Not Found", response.jsonPath().getString("error"));
    }


    @Test
    void shouldFindTwoLawCases(){
        repository.deleteAll();
        repository.addLawCase(first);
        repository.addLawCase(second);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[test1, test2]", response.jsonPath().getString("name"));
    }

    @DisplayName("DeleteById Test")
    @Test
    void shouldDeleteOneLawCase(){
        repository.deleteAll();
        LawCase saved = repository.addLawCase(first);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .delete(API + saved.getId())
                .then()
                .extract().response();


        Assertions.assertEquals(200, response.statusCode());


        Response response2 = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response2.statusCode());
        Assertions.assertEquals("[]", response2.jsonPath().getString("name"));
    }


    @Test
    void shouldDeleteTwoLawCases(){
        repository.deleteAll();
        repository.addLawCase(first);
        repository.addLawCase(second);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());

        Response response1 = given()
                .contentType(ContentType.JSON)
                .when()
                .delete(API)
                .then()
                .extract().response();

        System.out.println(response.jsonPath().prettyPrint());

        Assertions.assertEquals(200, response1.statusCode());

        Response response2 = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response2.statusCode());
    }

}
