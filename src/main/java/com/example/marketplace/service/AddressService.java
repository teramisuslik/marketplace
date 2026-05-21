package com.example.marketplace.service;

import com.example.marketplace.DTO.CreateAddressRequest;
import com.example.marketplace.DTO.UserAddressDTO;
import com.example.marketplace.entity.UserAddress;
import com.example.marketplace.repository.UserAddressRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository addressRepository;
    private final UserService userService;

    public List<UserAddressDTO> list(String authorization) {
        Long userId = userService.getUserid(authorization);
        return addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId).stream()
                .map(AddressService::toDto)
                .toList();
    }

    @Transactional
    public UserAddressDTO create(String authorization, CreateAddressRequest body) {
        Long userId = userService.getUserid(authorization);
        validate(body);
        boolean makeDefault = Boolean.TRUE.equals(body.getIsDefault())
                || addressRepository
                        .findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                        .isEmpty();
        if (makeDefault) {
            clearDefault(userId);
        }
        UserAddress addr = UserAddress.builder()
                .userId(userId)
                .city(body.getCity().trim())
                .street(body.getStreet().trim())
                .building(body.getBuilding().trim())
                .apartment(trimNullable(body.getApartment()))
                .postalCode(body.getPostalCode().trim())
                .isDefault(makeDefault)
                .createdAt(Instant.now())
                .build();
        return toDto(addressRepository.save(addr));
    }

    @Transactional
    public void delete(String authorization, Long addressId) {
        Long userId = userService.getUserid(authorization);
        UserAddress addr = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Адрес не найден"));
        addressRepository.delete(addr);
    }

    private void clearDefault(Long userId) {
        for (UserAddress a : addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)) {
            if (a.isDefault()) {
                a.setDefault(false);
                addressRepository.save(a);
            }
        }
    }

    private static void validate(CreateAddressRequest body) {
        if (body == null
                || isBlank(body.getCity())
                || isBlank(body.getStreet())
                || isBlank(body.getBuilding())
                || isBlank(body.getPostalCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заполните город, улицу, дом и индекс");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String trimNullable(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static UserAddressDTO toDto(UserAddress a) {
        UserAddressDTO dto = new UserAddressDTO();
        dto.setId(a.getId());
        dto.setCity(a.getCity());
        dto.setStreet(a.getStreet());
        dto.setBuilding(a.getBuilding());
        dto.setApartment(a.getApartment());
        dto.setPostalCode(a.getPostalCode());
        dto.setDefaultAddress(a.isDefault());
        return dto;
    }
}
