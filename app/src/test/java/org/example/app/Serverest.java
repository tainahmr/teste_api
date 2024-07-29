package org.example.app;

import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;

import static io.restassured.RestAssured.*;

public class Serverest {

    @BeforeAll
    public static void preCondition() {
        baseURI = "http://localhost";
        port = 3000;
    }

    @Test
    public void getUsuarios() {
        when()
                .get("/usuarios")
        .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void postUsuarios() {
        String userID = given()
                .body("{\n" +
                        "  \"nome\": \"Fulano da Silva\",\n" +
                        "  \"email\": \"restassurance01@qa.com.br\",\n" +
                        "  \"password\": \"teste\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

    }

    @Test
    public void exercicio03() {
        Faker faker = new Faker();
        String userName = faker.name().firstName();
        String userEmail = userName + "@restassured.com";
        String productName = faker.pokemon().name();
        Integer quantidadeInicial = 100;

        //Criando Usuário
        String userID = given()
                .body("{\n" +
                        "  \"nome\": \""+ userName + "\",\n" +
                        "  \"email\": \"" + userEmail + "\",\n" +
                        "  \"password\": \"teste\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is ("Cadastro realizado com sucesso"))
                .extract().path("_id");

        //Autenticando Usuário
        String userToken = given()
                .body("{\n" +
                        "  \"email\": \""+ userEmail +"\",\n" +
                        "  \"password\": \"teste\"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/login")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is ("Login realizado com sucesso"))
                .extract().path("authorization");

        //Cadastrar Produto
        String productId = given()
                .header("authorization", userToken)
                .body("{\n" +
                        "  \"nome\": \""+ productName +"\",\n" +
                        "  \"preco\": 470,\n" +
                        "  \"descricao\": \"Mouse\",\n" +
                        "  \"quantidade\":"+ quantidadeInicial +"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/produtos")
        .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is ("Cadastro realizado com sucesso"))
                .extract().path("_id");

        //Checar Estoque Inicial do Produto
        given()
                .pathParam("_id", productId)
        .when()
                .get("/produtos/{_id}")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("quantidade", is (quantidadeInicial));

        //Criar carrinho
        given()
                .header("authorization", userToken)
                .body("{\n" +
                        "  \"produtos\": [\n" +
                        "    {\n" +
                        "      \"idProduto\": \""+ productId +"\",\n" +
                        "      \"quantidade\": 1\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/carrinhos")
        .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is ("Cadastro realizado com sucesso"));

        //Checar Estoque Reduzido do Produto
        given()
                .pathParam("_id", productId)
        .when()
                .get("/produtos/{_id}")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("quantidade", is(quantidadeInicial - 1));

        //Cancelar Compra
        given()
                .header("authorization", userToken)
        .when()
                .delete("/carrinhos/cancelar-compra")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is ("Registro excluído com sucesso. Estoque dos produtos reabastecido"));

        //Checar Estoque Reabastecido do Produto
        given()
                .pathParam("_id", productId)
                .when()
                .get("/produtos/{_id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("quantidade", is (quantidadeInicial));

        //Excluindo Produto
        given()
                .pathParam("_id", productId)
                .header("authorization", userToken)
                .when()
                .delete("/produtos/{_id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));

        //EXCLUÍNDO USUÁRIO
        given()
                .pathParam("_id", userID)
                .when()
                .delete("/usuarios/{_id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));


    }
}
