package sg.edu.ntu.gamify_demo.models;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee")
public class Employee
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "jobtitle")
    private String jobtitle;

    @Column(name = "department")
    private String department;

    @OneToMany(mappedBy = "employee")
    private List<Redemption> redemptions;

    /**
     * Constructs an Employee object with the provided details.
     * @param id The unique identifier of the employee.
     * @param userName The name of the employee.
     * @param email The email of the employee.
     * @param jobtitle The job title of the employee.
     * @param department The department of the employee.
     * @param createdAt The timestamp of when the employee was created.
     * @param updatedAt The timestamp of when the employee was last updated.
     */
}