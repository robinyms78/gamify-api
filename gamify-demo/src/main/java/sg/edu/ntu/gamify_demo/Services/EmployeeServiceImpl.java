package sg.edu.ntu.gamify_demo.Services;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sg.edu.ntu.gamify_demo.interfaces.EmployeeService;
import sg.edu.ntu.gamify_demo.models.Employee;
import sg.edu.ntu.gamify_demo.exceptions.EmployeeNotFoundException;
import sg.edu.ntu.gamify_demo.exceptions.EmployeeValidationException;
import sg.edu.ntu.gamify_demo.repositories.EmployeeRepository;

@Primary
@Service
public class EmployeeServiceImpl implements EmployeeService 
{
    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) 
    {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public Employee createEmployee(Employee employee) 
    {
        
        EmployeeValidator.validateEmployee(employee);   //Validate the employee fields using the EmployeeValidator
        checkIfEmployeeExists(employee);                //Check if employee already exists based on unique fields (username or email)
        return employeeRepository.save(employee);       //Save and return the employee if no validation errors or duplicates
    }

    @Override
    public Optional<Employee> getEmployeeById(Long id) 
    {
        return employeeRepository.findById(id);
    }

    @Override
    public List<Employee> getAllEmployees() 
    {
        return employeeRepository.findAll(Sort.by(Sort.Order.asc("id")));   //Fetch employees sorted by ID in ascending order from the database
    }

    @Override
    public Employee updateEmployee(Long id, Employee employee) 
    {
        EmployeeValidator.validateEmployee(employee);   //Validate fields

        //Check if the employee exists
        if (!employeeRepository.existsById(id))
            throw new EmployeeNotFoundException("Employee not found with id: " + id);

        employee.setId(id);
        return employeeRepository.save(employee);
    }

    @Override
    public Employee partialUpdateEmployee(Long id, Employee employee) 
    {
        
        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(id);   //Check if the employee exists
        if (!existingEmployeeOpt.isPresent())
            throw new EmployeeNotFoundException("Employee not found with id: " + id);

        Employee existingEmployee = existingEmployeeOpt.get();

        //Partial update: Update only the non-null fields of the provided employee
        if (employee.getUsername() != null)
            existingEmployee.setUsername(employee.getUsername());

        if (employee.getEmail() != null)
            existingEmployee.setEmail(employee.getEmail());
    
        if (employee.getJobtitle() != null) 
            existingEmployee.setJobtitle(employee.getJobtitle());
        
        if (employee.getDepartment() != null) 
            existingEmployee.setDepartment(employee.getDepartment());

        //Save the updated entity
        return employeeRepository.save(existingEmployee);
    }

    @Override
    public void deleteEmployee(Long id) 
    {
        //Check if the employee exists
        if (!employeeRepository.existsById(id))
            throw new EmployeeNotFoundException("Employee not found with id: " + id);

        employeeRepository.deleteById(id);
    }

    //Check if an employee already exists by unique fields
    private void checkIfEmployeeExists(Employee employee) 
    {
        if (employeeRepository.existsByUsername(employee.getUsername()))
            throw new EmployeeValidationException("Username already exists");

        if (employeeRepository.existsByEmail(employee.getEmail()))
            throw new EmployeeValidationException("Email already exists");
    }
}