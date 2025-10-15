package com.chatapp.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.JwtResponse;
import com.chatapp.dto.LoginRequest;
import com.chatapp.dto.SignupRequest;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtUtils;
import com.chatapp.security.UserPrincipal;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    JwtUtils jwtUtils;
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Login attempt for user: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            
            // Update last login
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                logger.info("User logged in successfully: {}", userDetails.getUsername());
            }
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    user != null ? user.getDisplayName() : null));
                    
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt for user: {}", loginRequest.getUsername());
            Map<String, String> error = new HashMap<>();
            error.put("error", "ユーザー名またはパスワードが正しくありません");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (AuthenticationException e) {
            logger.error("Authentication error for user: {}", loginRequest.getUsername(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "認証処理中にエラーが発生しました");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (DataAccessException e) {
            logger.error("Database error during login for user: {}", loginRequest.getUsername(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "データベース接続エラーが発生しました");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try {
            logger.info("Registration attempt for user: {}", signUpRequest.getUsername());
            
            // Validate username
            if (signUpRequest.getUsername() == null || signUpRequest.getUsername().trim().length() < 3) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "ユーザー名は3文字以上で入力してください");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Validate password
            if (signUpRequest.getPassword() == null || signUpRequest.getPassword().length() < 6) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "パスワードは6文字以上で入力してください");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check if username already exists
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                logger.warn("Registration failed: Username already taken - {}", signUpRequest.getUsername());
                Map<String, String> error = new HashMap<>();
                error.put("error", "このユーザー名は既に使用されています");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Registration failed: Email already in use - {}", signUpRequest.getEmail());
                Map<String, String> error = new HashMap<>();
                error.put("error", "このメールアドレスは既に使用されています");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Create new user's account
            User user = new User(
                signUpRequest.getUsername().trim(),
                signUpRequest.getEmail().trim().toLowerCase(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getDisplayName() != null ? signUpRequest.getDisplayName().trim() : null
            );
            
            userRepository.save(user);
            logger.info("User registered successfully: {}", user.getUsername());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "アカウントが正常に作成されました");
            response.put("username", user.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            logger.error("Registration error for user: {}", signUpRequest.getUsername(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "アカウント作成中にエラーが発生しました");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // フロントエンドとの互換性のためのエイリアス
    @PostMapping("/signup")
    public ResponseEntity<?> signupUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return registerUser(signUpRequest);
    }
}