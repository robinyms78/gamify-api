// Java class for GamifyDemoApplication
// GamifyDemoApplication.java

package sg.edu.ntu.gamify_demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@MapperScan("sg.edu.ntu.gamify_demo.mappers")
public class GamifyDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(GamifyDemoApplication.class, args);
	}

	@GetMapping("/home")
	public String home() {
		return "Hello World!";
	}
}