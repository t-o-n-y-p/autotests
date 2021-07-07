package ru.levelp.at.hw9;

import static io.restassured.RestAssured.given;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveOAuth2HeaderScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.levelp.at.hw9.templates.Gender;
import ru.levelp.at.hw9.templates.Status;
import ru.levelp.at.hw9.templates.request.data.UserRequestData;
import ru.levelp.at.hw9.templates.response.ResponseBody;
import ru.levelp.at.hw9.templates.response.data.UserResponseData;

public class GoRestTest {

    private final List<Long> allCreatedUsers = new ArrayList<>();
    private final Faker faker = new Faker();

    @BeforeSuite
    public void setUp() {
        PreemptiveOAuth2HeaderScheme authenticationScheme = new PreemptiveOAuth2HeaderScheme();
        authenticationScheme.setAccessToken("3ae4f0a21a4d9d7043dd096d767bce4d8567437c411c849cb0a180f27c40fed9");

        RestAssured.baseURI = "https://gorest.co.in";
        RestAssured.requestSpecification = new RequestSpecBuilder()
            .log(LogDetail.ALL)
            .setContentType(ContentType.JSON)
            .setAuth(authenticationScheme)
            .build();

        RestAssured.responseSpecification = new ResponseSpecBuilder()
            .log(LogDetail.ALL)
            .build();
    }

    @AfterSuite
    public void tearDown() {
        for (Long id : allCreatedUsers) {
            given()
                .pathParam("id", id)
                .when()
                .delete("/public-api/users/{id}");
        }
    }

    @DataProvider
    private Object[][] getAddUserData() {
        Object[][] data = new Object[][] {
            {faker.name().fullName(), Gender.MALE, faker.internet().emailAddress(), Status.ACTIVE}
        };
        return Arrays.stream(data)
            .map(e -> new Object[]{
                UserRequestData.builder()
                    .name((String) e[0])
                    .gender((Gender) e[1])
                    .email((String) e[2])
                    .status((Status) e[3])
                    .build(),
                ResponseBody.<UserResponseData>builder()
                    .code(201)
                    .meta(null)
                    .data(
                        UserResponseData.builder()
                            .name((String) e[0])
                            .gender((Gender) e[1])
                            .email((String) e[2])
                            .status((Status) e[3])
                            .build()
                    )
                    .build()
            })
            .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getAddUserData")
    public void testAddUser(UserRequestData requestBody, ResponseBody<UserResponseData> expectedResponseBody) {
        ResponseBody<UserResponseData> actualResponseBody = given()
            .body(requestBody)
            .when()
            .post("/public-api/users")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>(){});

        allCreatedUsers.add(actualResponseBody.getData().getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(actualResponseBody)
                  .usingRecursiveComparison()
                  .ignoringExpectedNullFields()
                  .isEqualTo(expectedResponseBody);
            softly.assertThat(actualResponseBody)
                  .extracting(ResponseBody::getMeta)
                  .isNull();
            softly.assertThat(actualResponseBody)
                  .extracting(r -> r.getData().getId())
                  .isNotNull();
            softly.assertThat(actualResponseBody)
                  .extracting(r -> r.getData().getCreatedAt())
                  .isNotNull();
            softly.assertThat(actualResponseBody)
                  .extracting(r -> r.getData().getUpdatedAt())
                  .isNotNull();
        });
    }

}
