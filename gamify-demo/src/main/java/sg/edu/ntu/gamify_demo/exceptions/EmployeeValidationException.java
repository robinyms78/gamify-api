package sg.edu.ntu.gamify_demo.exceptions;

public class EmployeeValidationException extends RuntimeException 
{
    public EmployeeValidationException(String message) 
    {
        super(message);
    }
}