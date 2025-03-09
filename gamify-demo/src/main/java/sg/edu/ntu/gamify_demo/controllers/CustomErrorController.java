package sg.edu.ntu.gamify_demo.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Custom error controller to handle application errors and display appropriate error pages.
 * This controller overrides the default Spring Boot error handling to provide custom error pages.
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handles all error requests and routes them to the appropriate error page.
     * 
     * @param request The HTTP request that resulted in an error
     * @return The path to the appropriate error page
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // Get the error status code
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        // If no status code is available, default to the general error page
        if (status == null) {
            return "forward:/error/error.html";
        }
        
        // Convert status to integer
        int statusCode = Integer.parseInt(status.toString());
        
        // Route to specific error pages based on status code
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            return "forward:/error/404.html";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return "forward:/error/500.html";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return "forward:/error/403.html";
        }
        
        // Default to general error page for other status codes
        return "forward:/error/error.html";
    }
}
