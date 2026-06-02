package com.imap.person.application.service;

import com.imap.person.domain.dto.AddressDto;
import com.imap.person.domain.dto.CreateAddressRequest;
import com.imap.person.infrastructure.entity.PerAddressEntity;
import com.imap.person.infrastructure.repository.PerAddressJpaRepository;
import com.imap.person.infrastructure.repository.PerPersonJpaRepository;
import com.imap.person.infrastructure.tenant.TenantContextHolder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AddressService {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    private final PerAddressJpaRepository addressRepo;
    private final PerPersonJpaRepository  personRepo;

    public AddressService(PerAddressJpaRepository addressRepo,
                          PerPersonJpaRepository  personRepo) {
        this.addressRepo = addressRepo;
        this.personRepo  = personRepo;
    }

    public AddressDto create(UUID personId, CreateAddressRequest req) {
        personRepo.findById(personId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Person not found: " + personId));

        OffsetDateTime now = OffsetDateTime.now();
        PerAddressEntity e = new PerAddressEntity();
        e.setId(UUID.randomUUID());
        e.setPersonId(personId);
        e.setTenantId(TenantContextHolder.get());
        e.setAddressTypeId(req.addressTypeId());
        e.setStreet(req.street());
        e.setStreetNumber(req.streetNumber());
        e.setFloor(req.floor());
        e.setApartment(req.apartment());
        e.setCity(req.city());
        e.setProvinceId(req.provinceId());
        e.setCountryId(req.countryId());
        e.setPostalCode(req.postalCode());
        e.setValidFrom(req.validFrom());
        e.setValidTo(req.validTo());
        e.setLocation(buildPoint(req.latitude(), req.longitude()));
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);

        return toDto(addressRepo.save(e));
    }

    @Transactional(readOnly = true)
    public List<AddressDto> listByPerson(UUID personId) {
        return addressRepo.findByPersonIdAndActiveTrue(personId)
            .stream().map(this::toDto).toList();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    /** Returns a WGS-84 Point if both lat and lng are non-null, otherwise null. */
    private static Point buildPoint(Double lat, Double lng) {
        if (lat == null || lng == null) return null;
        // Coordinate(x=longitude, y=latitude) — PostGIS/JTS convention
        Point pt = GF.createPoint(new Coordinate(lng, lat));
        pt.setSRID(4326);
        return pt;
    }

    private AddressDto toDto(PerAddressEntity e) {
        Double lat = e.getLocation() != null ? e.getLocation().getY() : null;
        Double lng = e.getLocation() != null ? e.getLocation().getX() : null;
        return new AddressDto(
            e.getId(), e.getPersonId(),
            e.getAddressTypeId(),
            e.getStreet(), e.getStreetNumber(), e.getFloor(), e.getApartment(),
            e.getCity(), e.getProvinceId(), e.getCountryId(), e.getPostalCode(),
            e.getValidFrom(), e.getValidTo(),
            lat, lng,
            e.isActive(), e.getCreatedAt()
        );
    }
}
