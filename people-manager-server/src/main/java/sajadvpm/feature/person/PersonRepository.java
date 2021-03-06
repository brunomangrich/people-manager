package sajadvpm.feature.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PersonRepository  extends JpaRepository<Person, Integer> {

    @Query(value = "SELECT * FROM persons WHERE is_active = true", nativeQuery = true)
    List<Person> findAllByActive();

    @Query(value = "SELECT COUNT(*) FROM persons WHERE cpf = ?1", nativeQuery = true)
     Integer getCpfCount(String cpf);
}
