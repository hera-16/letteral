package com.chatapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // テストユーザーが存在しない場合のみ作成
        if (userRepository.findByUsername("user001").isEmpty()) {
            User testUser = new User();
            testUser.setUsername("user001");
            testUser.setPassword(passwordEncoder.encode("password"));
            testUser.setDisplayName("田中太郎");
            testUser.setEmail("tanaka@example.com");
            userRepository.save(testUser);
            
            System.out.println("テストユーザーを作成しました: username=user001, password=password");
        }
        
        if (userRepository.findByUsername("user002").isEmpty()) {
            User user2 = new User();
            user2.setUsername("user002");
            user2.setPassword(passwordEncoder.encode("password"));
            user2.setDisplayName("佐藤花子");
            user2.setEmail("sato@example.com");
            userRepository.save(user2);
            
            System.out.println("テストユーザーを作成しました: username=user002, password=password");
        }
        
        if (userRepository.findByUsername("user003").isEmpty()) {
            User user3 = new User();
            user3.setUsername("user003");
            user3.setPassword(passwordEncoder.encode("password"));
            user3.setDisplayName("鈴木一郎");
            user3.setEmail("suzuki@example.com");
            userRepository.save(user3);
            
            System.out.println("テストユーザーを作成しました: username=user003, password=password");
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