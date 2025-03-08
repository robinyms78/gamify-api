package sg.edu.ntu.gamify_demo.repositories;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import sg.edu.ntu.gamify_demo.models.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> 
{
    //Custom query methods to check if username or email already exists
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Employee> findByUsername(String username);
    Optional<Employee> findByEmail(String email);

    //Method to fetch all employees sorted by ID in ascending order
    @Override
    List<Employee> findAll(Sort sort);
}