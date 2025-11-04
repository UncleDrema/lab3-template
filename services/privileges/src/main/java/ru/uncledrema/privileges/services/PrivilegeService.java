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
        return privilegeRepository.findByUsername(username).orElseGet(() -> {
            var privilege = new Privilege(username, 0);
            return privilegeRepository.save(privilege);
        });
    }

    public Privilege withdraw(String username, UUID ticketId, Integer amount) {
        var privilege = getPrivilegeByUsername(username);
        privilege.withdraw(ticketId, amount);
        return privilegeRepository.save(privilege);
    }

    public Privilege deposit(String username, UUID ticketId, Integer amount) {
        var privilege = getPrivilegeByUsername(username);
        privilege.deposit(ticketId, amount);
        return privilegeRepository.save(privilege);
    }

    public Privilege cancel(String username, UUID ticketUid) {
        var privilege = getPrivilegeByUsername(username);
        privilege.cancel(ticketUid);
        return privilegeRepository.save(privilege);
    }
}
