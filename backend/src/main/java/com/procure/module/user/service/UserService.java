package com.procure.module.user.service;

import com.procure.common.exception.BusinessException;
import com.procure.module.company.entity.Branch;
import com.procure.module.company.entity.Company;
import com.procure.module.company.repository.BranchRepository;
import com.procure.module.company.repository.CompanyRepository;
import com.procure.module.user.dto.UserDtos.UserCreateRequest;
import com.procure.module.user.dto.UserDtos.UserResponse;
import com.procure.module.user.dto.UserDtos.UserUpdateRequest;
import com.procure.module.user.dto.UserDtos.UserProfileResponse;
import com.procure.module.user.entity.Permission;
import com.procure.module.user.entity.Role;
import com.procure.module.user.entity.User;
import com.procure.module.user.repository.RoleRepository;
import com.procure.module.user.repository.UserRepository;
import com.procure.module.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("Email is already registered: " + request.getEmail());
        }
        if (request.getEmployeeCode() != null && !request.getEmployeeCode().isBlank()
                && userRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw BusinessException.conflict("Employee code is already registered: " + request.getEmployeeCode());
        }

        Company company = null;
        if (request.getCompanyId() != null) {
            company = companyRepository.findById(request.getCompanyId())
                    .filter(c -> !c.isDeleted())
                    .orElseThrow(() -> BusinessException.notFound("Company"));
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            if (request.getCompanyId() == null) {
                throw new BusinessException("Company ID is required to link a branch");
            }
            branch = branchRepository.findByIdAndCompanyIdAndDeletedFalse(request.getBranchId(), request.getCompanyId())
                    .orElseThrow(() -> BusinessException.notFound("Branch"));
        }

        Set<Role> roles = fetchRoles(request.getRoles());

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .employeeCode(request.getEmployeeCode())
                .roles(roles)
                .company(company)
                .branch(branch)
                .status(User.UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);
        auditLogService.log("User", user.getId().toString(), "CREATE", null, user);
        return mapToResponse(user);
    }

    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> BusinessException.notFound("User"));

        User oldState = cloneState(user);

        Company company = null;
        if (request.getCompanyId() != null) {
            company = companyRepository.findById(request.getCompanyId())
                    .filter(c -> !c.isDeleted())
                    .orElseThrow(() -> BusinessException.notFound("Company"));
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            if (request.getCompanyId() == null) {
                throw new BusinessException("Company ID is required to link a branch");
            }
            branch = branchRepository.findByIdAndCompanyIdAndDeletedFalse(request.getBranchId(), request.getCompanyId())
                    .orElseThrow(() -> BusinessException.notFound("Branch"));
        }

        Set<Role> roles = fetchRoles(request.getRoles());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setEmployeeCode(request.getEmployeeCode());
        user.setRoles(roles);
        user.setCompany(company);
        user.setBranch(branch);
        user.setStatus(request.getStatus());

        user = userRepository.save(user);
        auditLogService.log("User", user.getId().toString(), "UPDATE", oldState, user);
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .map(this::mapToResponse)
                .orElseThrow(() -> BusinessException.notFound("User"));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> BusinessException.notFound("User"));

        user.setDeleted(true);
        userRepository.save(user);
        auditLogService.log("User", user.getId().toString(), "DELETE", null, null);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> BusinessException.notFound("User"));

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream().map(Permission::getName))
                .distinct()
                .toList();

        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setEmployeeCode(user.getEmployeeCode());
        response.setRoles(user.getRoles().stream().map(Role::getName).toList());
        response.setPermissions(permissions);

        if (user.getCompany() != null) {
            response.setCompanyId(user.getCompany().getId());
            response.setCompanyName(user.getCompany().getName());
        }
        if (user.getBranch() != null) {
            response.setBranchId(user.getBranch().getId());
            response.setBranchName(user.getBranch().getName());
        }

        return response;
    }

    private Set<Role> fetchRoles(List<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BusinessException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setEmployeeCode(user.getEmployeeCode());
        response.setStatus(user.getStatus());
        response.setRoles(user.getRoles().stream().map(Role::getName).toList());

        if (user.getCompany() != null) {
            response.setCompanyId(user.getCompany().getId());
            response.setCompanyName(user.getCompany().getName());
        }
        if (user.getBranch() != null) {
            response.setBranchId(user.getBranch().getId());
            response.setBranchName(user.getBranch().getName());
        }

        return response;
    }

    private User cloneState(User source) {
        return User.builder()
                .firstName(source.getFirstName())
                .lastName(source.getLastName())
                .email(source.getEmail())
                .phone(source.getPhone())
                .employeeCode(source.getEmployeeCode())
                .status(source.getStatus())
                .build();
    }
}
