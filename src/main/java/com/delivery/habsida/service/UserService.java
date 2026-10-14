package com.delivery.habsida.service;

import com.delivery.habsida.dto.MeResponse;
import com.delivery.habsida.dto.MeStoreResponse;
import com.delivery.habsida.entity.User;
import com.delivery.habsida.exception.UserNotFoundException;
import com.delivery.habsida.repository.UserRepository;
import com.delivery.habsida.repository.UserRoleRepository;
import com.delivery.habsida.repository.UserStoreAccessRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserStoreAccessRepository userStoreAccessRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserStoreAccessRepository userStoreAccessRepository,
                       UserRepository userRepository,
                       UserRoleRepository userRoleRepository) {
        this.userStoreAccessRepository = userStoreAccessRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public MeResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(a -> a.getRole().getName())
                .toList();

        List<MeStoreResponse> stores = userStoreAccessRepository.findAllByUserId(userId).stream()
                .map(a -> MeStoreResponse.from(a.getStore()))
                .toList();

        return new MeResponse(user.getId(), user.getEmail(), roles, stores);
    }
}
