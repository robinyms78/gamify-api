package sg.edu.ntu.gamify_demo.exceptions;

public class EmployeeNotFoundException extends RuntimeException 
{
    public EmployeeNotFoundException(String message) 
    {
        super(message);
    }
}