package VaadinDash;

/**
 * Created by Admin on 05.07.2017.
 */

import VaadinDash.repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class VaadinDashApplication implements CommandLineRunner {

    @Autowired
    private CounterRepository repository;


    public static void main(String[] args) {
        SpringApplication.run(VaadinDashApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {


    }



}