package org.youdzhin.auth;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(AuthServiceApplication.class, args);

    }
}


@RestController
@RequestMapping("/api/v1/auth/demo")
class DemoController {

    @GetMapping
    public ResponseEntity<String> sayHello () {
        return ResponseEntity.ok("hello from secured endpoint");
    }

}
