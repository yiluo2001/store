package de.yiluo.store.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void run_ShouldCreateAdmin_WhenAdminDoesNotExist() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        dataInitializer.run();

        verify(userService, times(1)).createUser(any(User.class));
    }

    @Test
    void run_ShouldNotCreateAdmin_WhenAdminAlreadyExists() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(new User()));

        dataInitializer.run();

        verify(userService, never()).createUser(any(User.class));
    }
}