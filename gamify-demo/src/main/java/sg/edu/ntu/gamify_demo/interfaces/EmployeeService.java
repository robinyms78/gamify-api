package sg.edu.ntu.gamify_demo.interfaces;
import java.util.List;
import java.util.Optional;

import sg.edu.ntu.gamify_demo.models.Employee;

public interface EmployeeService 
{
    Employee createEmployee(Employee employee);
    Optional<Employee> getEmployeeById(Long id);
    List<Employee> getAllEmployees();
    Employee updateEmployee(Long id, Employee employee);
    Employee partialUpdateEmployee(Long id, Employee employee);
    void deleteEmployee(Long id);
}