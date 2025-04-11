package com.backbase.audit.export;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.backbase.buildingblocks.test.http.TestRestTemplateConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("it")
class ApplicationTest {

    static {
        System.setProperty("SIG_SECRET_KEY", TestRestTemplateConfiguration.TEST_JWT_SIG_KEY);
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadContext() {
        assertNotNull(applicationContext);
    }

    @Test
    void shouldLoadContextWithArgs() {
        Application.main(new String[] {"--spring.profiles.active=it"});
        assertNotNull(applicationContext);
    }

}
