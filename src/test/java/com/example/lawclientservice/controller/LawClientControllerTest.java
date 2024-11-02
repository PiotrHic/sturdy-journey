package com.example.lawclientservice.controller;



import com.example.lawclientservice.domain.LawClient;
import com.example.lawclientservice.repository.InMemoryRepository;
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
public class LawClientControllerTest {

    @Autowired
    InMemoryRepository repository;

    @Autowired
    LawClientController lawClientController;

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


    private final String API = "/api/lawclient/";

    LawClient first = LawClient.builder()
            .id(1)
            .name("test")
            .build();

    LawClient second = LawClient.builder()
            .id(2)
            .name("test")
            .build();

    @Test
    void shouldSaveOneLawClient() {

        repository.deleteAll();

        String requestBody = "{\n" +
                "  \"id\": \"1\",\n" +
                "  \"name\": \"test\"}";

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
        Assertions.assertEquals("test", response.jsonPath().getString("name"));
    }


    @Test
    void updateOneLawClient(){
        repository.deleteAll();

        repository.addLawClient(first);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[test]", response.jsonPath().getString("name"));


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
    void shouldFindOneLawClient(){
        repository.deleteAll();
        LawClient saved = repository.addLawClient(first);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API + saved.getId())
                .then()
                .extract().response();

        System.out.println(response.jsonPath().prettyPrint());

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("test", response.jsonPath().getString("name"));
    }


    @Test
    void NotFoundLawClient(){
        repository.deleteAll();
        LawClient saved = repository.addLawClient(first);

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
    void shouldFindTwoLawClients(){
        repository.deleteAll();
        repository.addLawClient(first);
        repository.addLawClient(second);

        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(API)
                .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("[test, test]", response.jsonPath().getString("name"));
    }

    @DisplayName("DeleteById Test")
    @Test
    void shouldDeleteOneLawClient(){
        repository.deleteAll();
        LawClient saved = repository.addLawClient(first);

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
    void shouldDeleteTwoLawClients(){
        repository.deleteAll();
        repository.addLawClient(first);
        repository.addLawClient(second);

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