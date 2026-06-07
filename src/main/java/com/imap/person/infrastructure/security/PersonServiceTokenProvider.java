package com.imap.person.infrastructure.security;

import com.imap.platform.security.AbstractServiceTokenProvider;
import org.springframework.stereotype.Component;

/** Service-token provider de person (s2s). Lógica en AbstractServiceTokenProvider (imap-platform). */
@Component
public class PersonServiceTokenProvider extends AbstractServiceTokenProvider {
    @Override
    protected String microName() { return "person"; }
}
