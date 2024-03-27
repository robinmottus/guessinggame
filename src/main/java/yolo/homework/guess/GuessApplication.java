package yolo.homework.guess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class GuessApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(GuessApplication.class, args);}
}
