package org.example.assignment;

import org.example.entity.AssignmentMetadata;
import org.example.entity.Role;
import org.example.entity.User;

public class PermanentAssignment extends AbstractRoleAssignment {
    private boolean revoked;

    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
        this.revoked = false;
    }

    @Override
    public boolean isActive() {
        return !revoked;
    }

    @Override
    public String assignmentType() {
        return "PERMANENT";
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
