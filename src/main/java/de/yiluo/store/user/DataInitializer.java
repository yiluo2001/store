package de.yiluo.store.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    public DataInitializer(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    // String... is varargs, meaning zero or more string, or an array of string
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ROLE_ADMIN");
            userService.createUser(admin);
            System.out.println("Default admin user created: admin / admin123");
        }
    }
}