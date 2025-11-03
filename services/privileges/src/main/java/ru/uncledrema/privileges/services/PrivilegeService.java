package ru.uncledrema.privileges.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.uncledrema.privileges.types.Privilege;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PrivilegeService {
    private final PrivilegeRepository privilegeRepository;

    public Privilege getPrivilegeByUsername(String username) {
        return privilegeRepository.findByUsername(username);
    }

    public Privilege withdraw(String username, UUID ticketId, Integer amount) {
        var privilege = privilegeRepository.findByUsername(username);
        privilege.withdraw(ticketId, amount);
        return privilegeRepository.save(privilege);
    }

    public Privilege deposit(String username, UUID ticketId, Integer amount) {
        var privilege = privilegeRepository.findByUsername(username);
        privilege.deposit(ticketId, amount);
        return privilegeRepository.save(privilege);
    }

    public Privilege cancel(String username, UUID ticketUid) {
        var privilege = privilegeRepository.findByUsername(username);
        privilege.cancel(ticketUid);
        return privilegeRepository.save(privilege);
    }
}
