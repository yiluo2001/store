package de.yiluo.store.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// load Mockito so it can process annotations like @Mock and @InjectMocks
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // @Mock make it a mocking obj that does what you say
    // does nothing by default
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("tom");
        sampleUser.setPassword("rawPassword");
    }

    @Test
    void createUser_ShouldEncodePasswordAndSetDefaultRole_WhenRoleIsNull() {
        sampleUser.setRole(null);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        // any(User.class) means any User obj
        // invocation -> invocation.getArgument(0) get first argument, ie output the input
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser(sampleUser);

        assertNotNull(createdUser);
        assertEquals("encodedPassword", createdUser.getPassword());
        assertEquals("ROLE_USER", createdUser.getRole());
        // executed exactly once
        verify(userRepository, times(1)).save(sampleUser);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        sampleUser.setRole("ROLE_USER");
        // Optional.of(sampleUser) put it inside optional wrapper
        when(userRepository.findByUsername("tom")).thenReturn(Optional.of(sampleUser));

        String targetUsername = "tom";
        UserDetails userDetails = userService.loadUserByUsername(targetUsername);

        assertNotNull(userDetails);
        assertEquals(targetUsername, userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        String missingUsername = "ghost";
        when(userRepository.findByUsername(missingUsername)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(missingUsername);
        });
    }

    @Test
    void updateUserRole_ShouldChangeRoleAndSave_WhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        User updatedUser = userService.updateUserRole(1L, "ROLE_ADMIN");
        
        assertEquals("ROLE_ADMIN", updatedUser.getRole());
        verify(userRepository, times(1)).save(sampleUser);
    }
}