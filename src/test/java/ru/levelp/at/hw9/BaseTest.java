package ru.levelp.at.hw9;

import static io.restassured.RestAssured.given;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveOAuth2HeaderScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeSuite;

public abstract class BaseTest {

    protected static final String BASE_URL = "https://gorest.co.in";

    protected static final String USERS_API_URL = "/public/v1/users";
    protected static final String USERS_PAGE_API_URL = "/public/v1/users?page=%d";
    protected static final String USER_BY_ID_API_URL = "/public/v1/users/{id}";
    protected static final String USER_BY_ID_INVALID_INPUT_API_URL = "/public/v1/users/qwerty";

    protected static final String POSTS_API_URL = "/public/v1/posts";
    protected static final String POSTS_PAGE_API_URL = "/public/v1/posts?page=%d";
    protected static final String POST_BY_ID_API_URL = "/public/v1/posts/{id}";
    protected static final String POST_BY_ID_INVALID_INPUT_API_URL = "/public/v1/posts/qwerty";

    protected static final String COMMENTS_API_URL = "/public/v1/comments";
    protected static final String COMMENTS_PAGE_API_URL = "/public/v1/comments?page=%d";
    protected static final String COMMENT_BY_ID_API_URL = "/public/v1/comments/{id}";
    protected static final String COMMENT_BY_ID_INVALID_INPUT_API_URL = "/public/v1/comments/qwerty";

    protected final List<Long> allCreatedUsers = new ArrayList<>();
    protected final List<Long> allCreatedPosts = new ArrayList<>();
    protected final List<Long> allCreatedComments = new ArrayList<>();
    protected final Faker faker = new Faker();

    @BeforeSuite
    public void setUp() {
        PreemptiveOAuth2HeaderScheme authenticationScheme = new PreemptiveOAuth2HeaderScheme();
        authenticationScheme.setAccessToken("3ae4f0a21a4d9d7043dd096d767bce4d8567437c411c849cb0a180f27c40fed9");

        RestAssured.baseURI = BASE_URL;
        RestAssured.requestSpecification = new RequestSpecBuilder()
            .log(LogDetail.ALL)
            .setContentType(ContentType.JSON)
            .setAuth(authenticationScheme)
            .build();

        RestAssured.responseSpecification = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();
    }

    @AfterClass
    public void tearDown() {
        for (Long id : allCreatedUsers) {
            given()
                .pathParam("id", id)
                .when()
                .delete(USER_BY_ID_API_URL);
        }
        allCreatedUsers.clear();
        for (Long id : allCreatedPosts) {
            given()
                .pathParam("id", id)
                .when()
                .delete(POST_BY_ID_API_URL);
        }
        allCreatedPosts.clear();
        for (Long id : allCreatedComments) {
            given()
                .pathParam("id", id)
                .when()
                .delete(COMMENT_BY_ID_API_URL);
        }
        allCreatedComments.clear();
    }

}