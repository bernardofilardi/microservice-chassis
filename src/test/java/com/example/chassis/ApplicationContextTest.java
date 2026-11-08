package com.example.chassis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {
    @Test
    @DisplayName("Garante que o contexto do Spring Boot inicializa sem falhas de dependência ou configuração")
    void contextLoads() {
        // Se o Spring Container subir todos os beans sem exceções, o teste passa automaticamente.
        assertDoesNotThrow(() -> {
        });
    }

    @Test
    @DisplayName("Valida se a aplicação está executando sobre a JVM 21+")
    void shouldRunOnJava21OrHigher() {
        String javaVersion = System.getProperty("java.version");
        int majorVersion = Integer.parseInt(javaVersion.split("\\.")[0]);
        assertTrue(majorVersion >= 21, "A aplicação deve ser executada com a JDK 21 ou superior para suporte a Virtual Threads.");
    }
}