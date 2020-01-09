package com.transsnet.vskit.neptune;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * description：
 *
 * @author yangwei
 * @date 2019-06-14 17:00
 */
@Slf4j
@SpringBootApplication
public class VskitNeptuneConsoleApplication {
    public static void main(String[] args) {
        SpringApplication.run(VskitNeptuneConsoleApplication.class, args);
        log.info("App running at:");
        log.info("==>> http://localhost:9999/index.html");
    }
}
