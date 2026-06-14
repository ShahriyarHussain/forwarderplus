package com.lazoft.forwarderplus.services;

import com.lazoft.forwarderplus.entity.User;
import com.lazoft.forwarderplus.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final int MAX_ATTEMPTS = 3;

    private final UserRepository userRepository;

    public Optional<User> get(String id) {
        return userRepository.findById(id);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsUserByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsById(username);
    }

    public User update(User entity) {
        return userRepository.save(entity);
    }

    public void delete(String id) {
        userRepository.deleteById(id);
    }

    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> list(Pageable pageable, Specification<User> filter) {
        return userRepository.findAll(filter, pageable);
    }

    public void create(User user) {
        userRepository.save(user);
    }

    public int count() {
        return (int) userRepository.count();
    }

    public Page<User> getUsersByFilters(Pageable pageable, Specification<User> filter) {
        return userRepository.findAll(filter, pageable);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return get(username).orElseThrow(() ->
                new UsernameNotFoundException("No user present with username: " + username));
    }

    public void increaseFailedLoginCount(String username) {
        User user = get(username).orElseThrow(() -> new UsernameNotFoundException(""));
        user.setInvalidAttempts(user.getInvalidAttempts() + 1);
        user.setUserNotLocked(user.getInvalidAttempts() < MAX_ATTEMPTS);
        update(user);
    }

    public void resetFailedLoginCount(String username) {
        User user = get(username).orElseThrow(() -> new UsernameNotFoundException(""));
        user.setInvalidAttempts(0);
        update(user);
    }


}
