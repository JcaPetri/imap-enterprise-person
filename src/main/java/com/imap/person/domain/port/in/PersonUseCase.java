package com.imap.person.domain.port.in;

import com.imap.person.domain.dto.CreatePersonRequest;
import com.imap.person.domain.dto.PersonDto;
import com.imap.person.domain.dto.PersonSummaryDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonUseCase {
    PersonDto create(CreatePersonRequest request);
    Optional<PersonDto> findById(UUID id);
    List<PersonSummaryDto> search(String query);
    List<PersonSummaryDto> findAll();
    PersonDto deactivate(UUID id);
}
