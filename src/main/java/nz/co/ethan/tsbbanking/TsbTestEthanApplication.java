package nz.co.ethan.tsbbanking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("nz.co.ethan.tsbbanking.mapper")
public class TsbTestEthanApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsbTestEthanApplication.class, args);
    }

}
