package com.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.UserPrincipal;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // まずusernameで検索
        User user = userRepository.findByUsername(username).orElse(null);
        
        // usernameで見つからない場合、emailで検索（JWT subjectがemailの場合に対応）
        if (user == null) {
            user = userRepository.findByEmail(username).orElse(null);
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with username or email: " + username);
        }
        
        return UserPrincipal.create(user);
    }
}