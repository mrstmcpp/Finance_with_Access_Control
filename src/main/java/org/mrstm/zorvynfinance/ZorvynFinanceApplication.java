package org.mrstm.zorvynfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
public class ZorvynFinanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZorvynFinanceApplication.class, args);
    }

}
