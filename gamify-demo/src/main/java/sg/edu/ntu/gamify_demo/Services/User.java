package sg.edu.ntu.gamify_demo.Services;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User 
{
    public static final String HTTP_STATUS = null;
    
    //Instance variables
    private int userId, points;
    private String userName, email, passwordHash, role, department;
    private LocalDateTime createdAt, updatedAt;
}