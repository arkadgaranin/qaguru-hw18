package com.gmail.arkgaranin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.Map;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class DemowebshopTests extends TestBase {

  @Test
  @DisplayName("Успешная регистрация через api/UI")
  void registrationTest() {
    given()
        .contentType("application/x-www-form-urlencoded")
        .formParam("_RequestVerificationToken",
            "IkPUEjblyxfR15wGZtjbmA60v6lfPT-r825lZyUP1UnpRI0dhW7190TIDrzzMSvO0_" +
                "4h30FTF2Iw5fYE2aptViiFizpCV2eqwheE3JQ3k1Y1")
        .formParam("Gender", "M")
        .formParam("FirstName", "Arkadiy")
        .formParam("LastName", "Garanin")
        .formParam("Email", "arktest@test.com")
        .formParam("Password", "qwerty")
        .formParam("ConfirmPassword", "qwerty")
        .formParam("register-button", "Register")
        .when()
        .post("/register")
        .then()
        .statusCode(302);

    open("/registerresult/1");
    $(".registration-result-page").shouldHave(text("Your registration completed"));
  }

  @Test
  @DisplayName("Восстановление пароля через api")
  void passwordRecoveryTest() {
    given()
        .contentType("application/x-www-form-urlencoded")
        .body("Email=test@tets.ru&send-email=Recover")
        .when()
        .post("/passwordrecovery")
        .then()
        .statusCode(200);
  }

  @Test
  @DisplayName("Подписка на рассылку новостей через api")
  void signUpForNewsletterTest() {
    given()
        .contentType("application/x-www-form-urlencoded; charset=UTF-8")
        .body("email=testemail@test.ru")
        .when()
        .post("/subscribenewsletter")
        .then()
        .log().all()
        .statusCode(200)
        .body("Success", is(true), "Result",
            is("Thank you for signing up! A verification email has been sent. We appreciate your interest."));
  }

  @Test
  @DisplayName("Прохождение опроса неавторизованным через api")
  void passingPollUnauthUserTest() {
    given()
        .contentType("application/x-www-form-urlencoded; charset=UTF-8")
        .body("pollAnswerId=1")
        .when()
        .post("/poll/vote")
        .then()
        .log().all()
        .statusCode(200)
        .body("error", is("Only registered users can vote."));
  }

  @Test
  @DisplayName("Добавление и удаление из списка сравнения")
  void addToCompareListTest() {
    // Достаем куки из метода авторизации
    Map<String, String> authCookies =
        given()
            .contentType("application/x-www-form-urlencoded; charset=UTF-8")
            .formParam("Email", "arktest@test.com")
            .formParam("Password", "qwerty")
            .when()
            .post("/login")
            .then()
            .statusCode(302)
            .extract().cookies();

    String AuthToken = "NOPCOMMERCE.AUTH=" + authCookies.get("NOPCOMMERCE.AUTH") + ";";
    String ARRAffinity = "ARRAffinity=" + authCookies.get("ARRAffinity") + ";";
    String NopCustomer = "Nop.customer=" + authCookies.get("Nop.customer") + ";";

    // Логируемся на сайте с помощью подмены токена
    open("/Themes/DefaultClean/Content/images/logo.png");
    getWebDriver().manage().addCookie(
        new Cookie("NOPCOMMERCE.AUTH", authCookies.get("NOPCOMMERCE.AUTH")));
    open("");
    $(".account").shouldHave(text("arktest@test.com"));

    // Добавляем в список сравнения 2 компьютера и проверяем по статус коду, что они добавились
    int[] id = {31, 72};
    for (int i : id) {
      given()
          .cookie(ARRAffinity)
          .cookie(AuthToken)
          .cookie(NopCustomer)
          .when()
          .get(String.format("/compareproducts/add/%d", i))
          .then()
          .statusCode(200);
    }
  }
}
