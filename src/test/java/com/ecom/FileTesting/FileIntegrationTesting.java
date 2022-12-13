package com.ecom.FileTesting;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;


@QuarkusTest
public class FileIntegrationTesting {

    @Test
    public void GetAll(){
        given() .queryParam("sourceType", "inputfolder")
                .when().get("http://localhost:8080/api/v1/retrievefiles")
                .then().statusCode(200)
                .body("status", equalTo("ok"))
                .body("responseCode", equalTo("200"))
                .body("responseDescription", equalTo("File retrieved"));

    }
    @Test
    public void getById(){
        given().queryParam("fileId","Dealer Contract_2022-10-28 05.10.12.328")
                .queryParam("fileId","Dealer Contract_2022-10-28 05.10.12.328")
                .when().get("http://localhost:8080/api/v1/byteStreamByFileID")
                .then().statusCode(200)
                .body("status", equalTo("ok"))
                .body("responseCode", equalTo("200"))
                .body("responseDescription", equalTo("File retrieved"));

    }

}
