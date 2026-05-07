package com.example.ExpenseTracker.service;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;


@Service
public class AuthLoading {

    @Bean
    public LoadingCache<String, User> userLoad(UserRepository userRepository){
        return Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build(userRepository::findByEmail);
    }
}
