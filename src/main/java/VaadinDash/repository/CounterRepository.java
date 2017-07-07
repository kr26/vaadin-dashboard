package VaadinDash.repository;

import VaadinDash.entity.DashCounter;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Admin on 05.07.2017.
 */
public interface CounterRepository extends MongoRepository<DashCounter, String>{




}
