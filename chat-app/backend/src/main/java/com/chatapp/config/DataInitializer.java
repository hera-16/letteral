package com.chatapp.config;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // テストユーザーが存在しない場合のみ作成
        if (userRepository.findByUsername("testuser").isEmpty()) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setPassword(passwordEncoder.encode("password"));
            testUser.setDisplayName("テストユーザー");
            testUser.setEmail("test@example.com");
            userRepository.save(testUser);
            
            System.out.println("テストユーザーを作成しました: username=testuser, password=password");
        }
        
        if (userRepository.findByUsername("admin").isEmpty()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setDisplayName("管理者");
            adminUser.setEmail("admin@example.com");
            userRepository.save(adminUser);
            
            System.out.println("管理者ユーザーを作成しました: username=admin, password=admin123");
        }
    }
}