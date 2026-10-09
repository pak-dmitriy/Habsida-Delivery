package com.delivery.habsida.security;

import com.delivery.habsida.repository.UserStoreAccessRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreAccessGuardTest {

    @Mock
    private UserStoreAccessRepository userStoreAccessRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private StoreAccessGuard storeAccessGuard;

    @Test
    void shouldReturnTrue_whenUserIsAdmin() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();

        boolean result = storeAccessGuard.canAccessStore(authentication, 1L);

        assertTrue(result);
    }

    @Test
    void shouldReturnTrue_whenMerchantHasAccessToStore() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(new UserPrincipal(1L, "merchant@test.com"));
        when(userStoreAccessRepository.existsByUserIdAndStoreId(1L, 5L))
                .thenReturn(true);

        boolean result = storeAccessGuard.canAccessStore(authentication, 5L);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenMerchantHasNoAccessToStore() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")))
                .when(authentication).getAuthorities();
        when(authentication.getPrincipal())
                .thenReturn(new UserPrincipal(1L, "merchant@test.com"));
        when(userStoreAccessRepository.existsByUserIdAndStoreId(1L, 5L))
                .thenReturn(false);

        boolean result = storeAccessGuard.canAccessStore(authentication, 5L);

        assertFalse(result);
    }
}