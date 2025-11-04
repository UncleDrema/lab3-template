package ru.uncledrema.privileges;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.uncledrema.privileges.services.PrivilegeRepository;
import ru.uncledrema.privileges.services.PrivilegeService;
import ru.uncledrema.privileges.types.Privilege;
import ru.uncledrema.privileges.types.PrivilegeStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrivilegesApplicationTests {

    private PrivilegeRepository privilegeRepository;
    private PrivilegeService privilegeService;

    @BeforeEach
    void setUp() {
        privilegeRepository = mock(PrivilegeRepository.class);
        privilegeService = new PrivilegeService(privilegeRepository);
    }

    @Test
    void getPrivilegeByUsername_returnsPrivilege() {
        Privilege privilege = new Privilege("user", PrivilegeStatus.BRONZE, 100);
        when(privilegeRepository.findByUsername("user")).thenReturn(Optional.of(privilege));

        Privilege result = privilegeService.getPrivilegeByUsername("user");
        assertEquals(privilege, result);
    }

    @Test
    void withdraw_decreasesBalance() {
        Privilege privilege = new Privilege("user", PrivilegeStatus.BRONZE, 100);
        when(privilegeRepository.findByUsername("user")).thenReturn(Optional.of(privilege));
        when(privilegeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Privilege result = privilegeService.withdraw("user", UUID.randomUUID(), 50);
        assertEquals(50, result.getBalance());
    }

    @Test
    void deposit_increasesBalance() {
        Privilege privilege = new Privilege("user", PrivilegeStatus.BRONZE, 100);
        when(privilegeRepository.findByUsername("user")).thenReturn(Optional.of(privilege));
        when(privilegeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Privilege result = privilegeService.deposit("user", UUID.randomUUID(), 30);
        assertEquals(130, result.getBalance());
    }

    @Test
    void cancel_revertsOperation() {
        Privilege privilege = new Privilege("user", PrivilegeStatus.BRONZE, 100);
        UUID ticketUid = UUID.randomUUID();
        privilege.deposit(ticketUid, 50);
        when(privilegeRepository.findByUsername("user")).thenReturn(Optional.of(privilege));
        when(privilegeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Privilege result = privilegeService.cancel("user", ticketUid);
        assertEquals(100, result.getBalance());
    }

}
