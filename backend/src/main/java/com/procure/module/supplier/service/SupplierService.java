package com.procure.module.supplier.service;

import com.procure.common.exception.ResourceNotFoundException;
import com.procure.module.supplier.dto.SupplierDtos.*;
import com.procure.module.supplier.entity.Supplier;
import com.procure.module.supplier.entity.Supplier.SupplierStatus;
import com.procure.module.supplier.entity.SupplierContact;
import com.procure.module.supplier.repository.SupplierContactRepository;
import com.procure.module.supplier.repository.SupplierRepository;
import com.procure.module.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository        supplierRepository;
    private final SupplierContactRepository contactRepository;
    private final CompanyRepository         companyRepository;

    // ── CREATE ────────────────────────────────────────────────────

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByCodeAndIsDeletedFalse(request.code())) {
            throw new IllegalArgumentException("Supplier code '" + request.code() + "' already exists");
        }

        var company = companyRepository.findByIdAndIsDeletedFalse(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.companyId()));

        Supplier supplier = Supplier.builder()
                .name(request.name())
                .code(request.code().toUpperCase())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .city(request.city())
                .country(request.country())
                .taxNumber(request.taxNumber())
                .registrationNumber(request.registrationNumber())
                .website(request.website())
                .paymentTerms(request.paymentTerms())
                .creditLimit(request.creditLimit())
                .status(request.status() != null ? request.status() : SupplierStatus.ACTIVE)
                .company(company)
                .build();

        supplier = supplierRepository.save(supplier);
        syncContacts(supplier, request.contacts());

        log.info("Supplier created: {} [{}]", supplier.getName(), supplier.getId());
        return toResponse(supplierRepository.findByIdAndIsDeletedFalse(supplier.getId())
                .orElseThrow());
    }

    // ── UPDATE ────────────────────────────────────────────────────

    @Transactional
    public SupplierResponse updateSupplier(UUID id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        if (!supplier.getCode().equalsIgnoreCase(request.code()) &&
                supplierRepository.existsByCodeAndIsDeletedFalseAndIdNot(request.code(), id)) {
            throw new IllegalArgumentException("Supplier code '" + request.code() + "' already in use");
        }

        supplier.setName(request.name());
        supplier.setCode(request.code().toUpperCase());
        supplier.setEmail(request.email());
        supplier.setPhone(request.phone());
        supplier.setAddress(request.address());
        supplier.setCity(request.city());
        supplier.setCountry(request.country());
        supplier.setTaxNumber(request.taxNumber());
        supplier.setRegistrationNumber(request.registrationNumber());
        supplier.setWebsite(request.website());
        supplier.setPaymentTerms(request.paymentTerms());
        supplier.setCreditLimit(request.creditLimit());
        if (request.status() != null) supplier.setStatus(request.status());

        supplierRepository.save(supplier);
        syncContacts(supplier, request.contacts());

        log.info("Supplier updated: {} [{}]", supplier.getName(), id);
        return toResponse(supplierRepository.findByIdAndIsDeletedFalse(id).orElseThrow());
    }

    // ── RATE ──────────────────────────────────────────────────────

    @Transactional
    public SupplierResponse rateSupplier(UUID id, RatingRequest request) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));

        supplier.setRating(request.rating());
        supplierRepository.save(supplier);

        log.info("Supplier {} rated: {} stars", id, request.rating());
        return toResponse(supplier);
    }

    // ── READ ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(UUID id) {
        return toResponse(
                supplierRepository.findByIdAndIsDeletedFalse(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Supplier", id))
        );
    }

    @Transactional(readOnly = true)
    public Page<SupplierSummary> searchSuppliers(UUID companyId, String search,
                                                  SupplierStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return supplierRepository.searchByCompany(companyId, search, status, pageable)
                .map(this::toSummary);
    }

    // ── DELETE ────────────────────────────────────────────────────

    @Transactional
    public void deleteSupplier(UUID id) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
        supplier.setDeleted(true);
        supplierRepository.save(supplier);
        log.info("Supplier soft-deleted: {}", id);
    }

    // ── CONTACTS ──────────────────────────────────────────────────

    @Transactional
    public SupplierResponse addContact(UUID supplierId, ContactRequest request) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));

        if (request.primary()) {
            contactRepository.clearPrimaryForSupplier(supplierId);
        }

        SupplierContact contact = SupplierContact.builder()
                .supplier(supplier)
                .name(request.name())
                .designation(request.designation())
                .email(request.email())
                .phone(request.phone())
                .primary(request.primary())
                .build();

        contactRepository.save(contact);
        return toResponse(supplierRepository.findByIdAndIsDeletedFalse(supplierId).orElseThrow());
    }

    @Transactional
    public void deleteContact(UUID supplierId, UUID contactId) {
        SupplierContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", contactId));
        contact.setDeleted(true);
        contactRepository.save(contact);
    }

    // ── MAPPERS ───────────────────────────────────────────────────

    private void syncContacts(Supplier supplier, List<ContactRequest> contactRequests) {
        if (contactRequests == null || contactRequests.isEmpty()) return;

        boolean hasPrimary = contactRequests.stream().anyMatch(ContactRequest::primary);
        if (hasPrimary) {
            contactRepository.clearPrimaryForSupplier(supplier.getId());
        }

        for (ContactRequest cr : contactRequests) {
            if (cr.id() != null) {
                // Update existing
                contactRepository.findById(cr.id()).ifPresent(existing -> {
                    existing.setName(cr.name());
                    existing.setDesignation(cr.designation());
                    existing.setEmail(cr.email());
                    existing.setPhone(cr.phone());
                    existing.setPrimary(cr.primary());
                    contactRepository.save(existing);
                });
            } else {
                // Create new
                SupplierContact contact = SupplierContact.builder()
                        .supplier(supplier)
                        .name(cr.name())
                        .designation(cr.designation())
                        .email(cr.email())
                        .phone(cr.phone())
                        .primary(cr.primary())
                        .build();
                contactRepository.save(contact);
            }
        }
    }

    private SupplierResponse toResponse(Supplier s) {
        List<ContactResponse> contacts = contactRepository
                .findBySupplierIdAndIsDeletedFalse(s.getId())
                .stream().map(this::toContactResponse).toList();

        return new SupplierResponse(
                s.getId(), s.getName(), s.getCode(), s.getEmail(),
                s.getPhone(), s.getAddress(), s.getCity(), s.getCountry(),
                s.getTaxNumber(), s.getRegistrationNumber(), s.getWebsite(),
                s.getPaymentTerms(), s.getCreditLimit(), s.getRating(),
                s.getStatus(),
                s.getCompany() != null ? s.getCompany().getId() : null,
                contacts,
                s.getCreatedAt(), s.getUpdatedAt(), s.getCreatedBy()
        );
    }

    private SupplierSummary toSummary(Supplier s) {
        int contactCount = contactRepository.findBySupplierIdAndIsDeletedFalse(s.getId()).size();
        return new SupplierSummary(
                s.getId(), s.getName(), s.getCode(), s.getEmail(),
                s.getPhone(), s.getCity(), s.getCountry(),
                s.getRating(), s.getStatus(), contactCount
        );
    }

    private ContactResponse toContactResponse(SupplierContact c) {
        return new ContactResponse(
                c.getId(), c.getName(), c.getDesignation(),
                c.getEmail(), c.getPhone(), c.isPrimary()
        );
    }
}
