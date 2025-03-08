package sg.edu.ntu.gamify_demo.Services;
import sg.edu.ntu.gamify_demo.models.Employee;
import sg.edu.ntu.gamify_demo.exceptions.EmployeeValidationException;

public class EmployeeValidator 
{
    //Method to validate employee fields
    public static void validateEmployee(Employee employee) 
    {
        validateUsername(employee.getUsername());
        validateEmail(employee.getEmail());
        validateJobTitle(employee.getJobtitle());
        validateDepartment(employee.getDepartment());
    }

    //Method to validate username
    public static void validateUsername(String username) 
    {
        if (username == null || username.isEmpty())
            throw new EmployeeValidationException("Username is required");
    }

    //Method to validate email
    public static void validateEmail(String email) 
    {
        if (email == null || email.isEmpty())
            throw new EmployeeValidationException("Email is required");
    }

    //Method to validate job title
    public static void validateJobTitle(String jobTitle) 
    {
        if (jobTitle == null || jobTitle.isEmpty())
            throw new EmployeeValidationException("Job Title is required");
    }

    //Method to validate department
    public static void validateDepartment(String department) 
    {
        if (department == null || department.isEmpty())
            throw new EmployeeValidationException("Department is required");
    }
}