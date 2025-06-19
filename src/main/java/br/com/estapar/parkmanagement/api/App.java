package br.com.estapar.parkmanagement.api;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;

@OpenAPIDefinition(
    info = @Info(
            title = "api-park-management",
            version = "0.0"
    )
)
public class App {

    public static void main(String[] args) {
        System.setProperty("micronaut.server.port", "3003");
        Micronaut.run(App.class, args);
    }
}