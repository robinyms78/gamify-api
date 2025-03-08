package sg.edu.ntu.gamify_demo.controllers;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.ntu.gamify_demo.models.Employee;
import sg.edu.ntu.gamify_demo.exceptions.EmployeeNotFoundException;
import sg.edu.ntu.gamify_demo.interfaces.EmployeeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/employee")
public class EmployeeController 
{
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) 
    {
        this.employeeService = employeeService;
    }

    //Create Employee
    @PostMapping
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody Employee employee) 
    {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(createdEmployee, HttpStatus.CREATED);
    }

    //Get Employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) 
    {
        //Using a service to fetch the employee or throw an exception if not found
        Employee employee = employeeService.getEmployeeById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found with id: " + id));

        return ResponseEntity.ok(employee);
    }

    //Get all Employees
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() 
    {
        List<Employee> employees = employeeService.getAllEmployees();
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    //Update Employee (Full Update)
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee) 
    {
        try
        {
            Employee updatedEmployee = employeeService.updateEmployee(id, employee);
            return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
        } 
        
        catch (RuntimeException e) 
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //Partial Update Employee
    @PatchMapping("/{id}")
    public ResponseEntity<Employee> partialUpdateEmployee(@PathVariable Long id, @RequestBody Employee employee) 
    {
        try 
        {
            Employee updatedEmployee = employeeService.partialUpdateEmployee(id, employee);
            return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
        } 
        
        catch (RuntimeException e) 
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //Delete Employee
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) 
    {
        try 
        {
            employeeService.deleteEmployee(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } 
        
        catch (RuntimeException e) 
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}