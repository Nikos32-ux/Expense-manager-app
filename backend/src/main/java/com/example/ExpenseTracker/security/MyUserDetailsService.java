package com.example.ExpenseTracker.security;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.repository.UserRepository;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoadingCache<String, User> cache;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user {}", email);
        User user = cache.get(email);

        if(user == null) {
            log.warn("User not found {}", email);
            throw new UsernameNotFoundException("User not found");
        }
       return new UserPrincipal(user);
    }
}