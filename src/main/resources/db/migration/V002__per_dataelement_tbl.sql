-- =============================================================================
-- Person V002 — per_dataelement_tbl (catálogos EAV + seeds)
-- =============================================================================

CREATE TABLE person.per_dataelement_tbl (
    id               UUID         NOT NULL DEFAULT gen_random_uuid(),
    tenant_id        UUID,
    category         VARCHAR(50)  NOT NULL,
    code             VARCHAR(50)  NOT NULL,
    label_es         VARCHAR(200) NOT NULL,
    label_en         VARCHAR(200),
    label_pt         VARCHAR(200),
    parent_id        UUID,
    sort_order       SMALLINT     DEFAULT 0,
    is_active        BOOLEAN      NOT NULL DEFAULT true,
    -- Campos estándar IMAP
    state_id         UUID,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by_id    UUID,
    updated_by_id    UUID,
    owned_by_id      UUID,
    timezone_id      UUID,
    table_history    TEXT,
    data_language_id UUID,
    CONSTRAINT per_dataelement_pkey PRIMARY KEY (id),
    CONSTRAINT per_dataelement_category_code_uq UNIQUE (tenant_id, category, code)
);

CREATE INDEX idx_per_dataelement_category ON person.per_dataelement_tbl(category) WHERE is_active = true;
CREATE INDEX idx_per_dataelement_tenant    ON person.per_dataelement_tbl(tenant_id, category) WHERE is_active = true;

-- =============================================================================
-- SEEDS — datos de plataforma (tenant_id IS NULL)
-- =============================================================================

-- ADDRESS_TYPE
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0001-0000-0000-000000000001', 'ADDRESS_TYPE', 'FISCAL',     'Fiscal',     'Fiscal',      'Fiscal',      1),
    ('00000200-0001-0000-0000-000000000002', 'ADDRESS_TYPE', 'LEGAL',      'Legal',       'Legal',       'Legal',       2),
    ('00000200-0001-0000-0000-000000000003', 'ADDRESS_TYPE', 'COMERCIAL',  'Comercial',   'Commercial',  'Comercial',   3),
    ('00000200-0001-0000-0000-000000000004', 'ADDRESS_TYPE', 'REAL',       'Real',        'Real',        'Real',        4),
    ('00000200-0001-0000-0000-000000000005', 'ADDRESS_TYPE', 'POSTAL',     'Postal',      'Postal',      'Postal',      5),
    ('00000200-0001-0000-0000-000000000006', 'ADDRESS_TYPE', 'SUCURSAL',   'Sucursal',    'Branch',      'Filial',      6);

-- CONTACT_TYPE
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0002-0000-0000-000000000001', 'CONTACT_TYPE', 'EMAIL',            'Email',         'Email',         'Email',         1),
    ('00000200-0002-0000-0000-000000000002', 'CONTACT_TYPE', 'TELEFONO_FIJO',    'Teléfono Fijo', 'Phone',         'Telefone Fixo', 2),
    ('00000200-0002-0000-0000-000000000003', 'CONTACT_TYPE', 'TELEFONO_MOVIL',   'Teléfono Móvil','Mobile',        'Celular',       3),
    ('00000200-0002-0000-0000-000000000004', 'CONTACT_TYPE', 'FAX',              'Fax',           'Fax',           'Fax',           4),
    ('00000200-0002-0000-0000-000000000005', 'CONTACT_TYPE', 'WHATSAPP',         'WhatsApp',      'WhatsApp',      'WhatsApp',      5),
    ('00000200-0002-0000-0000-000000000006', 'CONTACT_TYPE', 'LINKEDIN',         'LinkedIn',      'LinkedIn',      'LinkedIn',      6),
    ('00000200-0002-0000-0000-000000000007', 'CONTACT_TYPE', 'WEB',              'Sitio Web',     'Website',       'Site Web',      7);

-- CONTACT_LABEL
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0003-0000-0000-000000000001', 'CONTACT_LABEL', 'FACTURACION',    'Facturación',   'Billing',       'Faturamento',   1),
    ('00000200-0003-0000-0000-000000000002', 'CONTACT_LABEL', 'GERENCIA',       'Gerencia',      'Management',    'Gerência',      2),
    ('00000200-0003-0000-0000-000000000003', 'CONTACT_LABEL', 'VENTAS',         'Ventas',        'Sales',         'Vendas',        3),
    ('00000200-0003-0000-0000-000000000004', 'CONTACT_LABEL', 'COMPRAS',        'Compras',       'Purchasing',    'Compras',       4),
    ('00000200-0003-0000-0000-000000000005', 'CONTACT_LABEL', 'TECNICO',        'Técnico',       'Technical',     'Técnico',       5),
    ('00000200-0003-0000-0000-000000000006', 'CONTACT_LABEL', 'ADMINISTRACION', 'Administración','Administration','Administração', 6);

-- ENTITY_TYPE
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0004-0000-0000-000000000001', 'ENTITY_TYPE', 'SA',                  'Sociedad Anónima',              'Corporation',                  'Sociedade Anônima',             1),
    ('00000200-0004-0000-0000-000000000002', 'ENTITY_TYPE', 'SRL',                 'Sociedad de Resp. Limitada',    'Limited Liability Co.',         'Sociedade Limitada',            2),
    ('00000200-0004-0000-0000-000000000003', 'ENTITY_TYPE', 'SAS',                 'Sociedad por Acciones Simpl.',  'Simplified Joint-Stock Co.',    'Sociedade Simplificada',        3),
    ('00000200-0004-0000-0000-000000000004', 'ENTITY_TYPE', 'COOPERATIVA',         'Cooperativa',                   'Cooperative',                   'Cooperativa',                   4),
    ('00000200-0004-0000-0000-000000000005', 'ENTITY_TYPE', 'ASOCIACION_CIVIL',    'Asociación Civil',              'Non-Profit Association',        'Associação Civil',              5),
    ('00000200-0004-0000-0000-000000000006', 'ENTITY_TYPE', 'SOCIEDAD_SIMPLE',     'Sociedad Simple',               'Simple Partnership',            'Sociedade Simples',             6),
    ('00000200-0004-0000-0000-000000000007', 'ENTITY_TYPE', 'SOCIEDAD_COLECTIVA',  'Sociedad Colectiva',            'General Partnership',           'Sociedade Coletiva',            7),
    ('00000200-0004-0000-0000-000000000008', 'ENTITY_TYPE', 'FUNDACION',           'Fundación',                     'Foundation',                    'Fundação',                      8),
    ('00000200-0004-0000-0000-000000000009', 'ENTITY_TYPE', 'EMPRESA_UNIPERSONAL', 'Empresa Unipersonal',           'Sole Proprietorship',           'Empresa Individual',            9),
    ('00000200-0004-0000-0000-000000000010', 'ENTITY_TYPE', 'UTE',                 'Unión Transitoria de Empresas', 'Joint Venture',                 'Joint Venture Temporário',     10),
    ('00000200-0004-0000-0000-000000000011', 'ENTITY_TYPE', 'FIDEICOMISO',         'Fideicomiso',                   'Trust',                         'Fideicomisso',                 11);

-- REGISTRATION_BODY
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0005-0000-0000-000000000001', 'REGISTRATION_BODY', 'IGJ',      'IGJ (CABA)',       'IGJ (CABA)',      'IGJ (CABA)',       1),
    ('00000200-0005-0000-0000-000000000002', 'REGISTRATION_BODY', 'RPC_CBA',  'RPC Córdoba',      'RPC Córdoba',     'RPC Córdoba',      2),
    ('00000200-0005-0000-0000-000000000003', 'REGISTRATION_BODY', 'RPC_BA',   'RPC Bs. As.',      'RPC Buenos Aires','RPC Buenos Aires',  3),
    ('00000200-0005-0000-0000-000000000004', 'REGISTRATION_BODY', 'RPC_MZA',  'RPC Mendoza',      'RPC Mendoza',     'RPC Mendoza',      4),
    ('00000200-0005-0000-0000-000000000005', 'REGISTRATION_BODY', 'RPC_SF',   'RPC Santa Fe',     'RPC Santa Fe',    'RPC Santa Fe',     5),
    ('00000200-0005-0000-0000-000000000006', 'REGISTRATION_BODY', 'RPC_TUC',  'RPC Tucumán',      'RPC Tucumán',     'RPC Tucumán',      6),
    ('00000200-0005-0000-0000-000000000007', 'REGISTRATION_BODY', 'OTRO',     'Otro',             'Other',           'Outro',            7);

-- BANK (principales Argentina — código BCRA + nombre)
INSERT INTO person.per_dataelement_tbl (category, code, label_es, sort_order) VALUES
    ('BANK', 'BCRA_007', 'Banco de Galicia y Buenos Aires S.A.U.',     1),
    ('BANK', 'BCRA_011', 'Banco de la Nación Argentina',               2),
    ('BANK', 'BCRA_014', 'Banco de la Provincia de Buenos Aires',      3),
    ('BANK', 'BCRA_015', 'Industrial and Commercial Bank of China',    4),
    ('BANK', 'BCRA_017', 'Banco Credicoop Cooperativo Limitado',       5),
    ('BANK', 'BCRA_018', 'Banco de Valores S.A.',                      6),
    ('BANK', 'BCRA_020', 'Banco Río de la Plata S.A.',                 7),
    ('BANK', 'BCRA_027', 'Banco Supervielle S.A.',                     8),
    ('BANK', 'BCRA_029', 'Banco de la Ciudad de Buenos Aires',         9),
    ('BANK', 'BCRA_034', 'Banco Patagonia S.A.',                      10),
    ('BANK', 'BCRA_044', 'Banco Hipotecario S.A.',                    11),
    ('BANK', 'BCRA_045', 'Banco San Juan S.A.',                       12),
    ('BANK', 'BCRA_060', 'Banco del Tucumán S.A.',                    13),
    ('BANK', 'BCRA_065', 'Banco Municipal de Rosario',                14),
    ('BANK', 'BCRA_072', 'Banco Santander Argentina S.A.',            15),
    ('BANK', 'BCRA_083', 'Banco del Chubut S.A.',                     16),
    ('BANK', 'BCRA_086', 'Banco de Santa Cruz S.A.',                  17),
    ('BANK', 'BCRA_093', 'Banco de la Pampa Soc. de Eco. Mixta',      18),
    ('BANK', 'BCRA_094', 'Banco de Corrientes S.A.',                  19),
    ('BANK', 'BCRA_097', 'Banco Provincia del Neuquén S.A.',          20),
    ('BANK', 'BCRA_143', 'Brubank S.A.U.',                            21),
    ('BANK', 'BCRA_147', 'Banco Interfinanzas S.A.',                  22),
    ('BANK', 'BCRA_150', 'HSBC Bank Argentina S.A.',                  23),
    ('BANK', 'BCRA_158', 'Openbank S.A.',                             24),
    ('BANK', 'BCRA_165', 'JPMorgan Chase Bank, N.A.',                 25),
    ('BANK', 'BCRA_191', 'Citibank N.A.',                             26),
    ('BANK', 'BCRA_198', 'Deutsche Bank S.A.',                        27),
    ('BANK', 'BCRA_247', 'Banco省 S.A.',                             28),
    ('BANK', 'BCRA_254', 'Nuevo Banco de Santa Fe S.A.',              29),
    ('BANK', 'BCRA_259', 'Banco ICBC S.A.',                           30),
    ('BANK', 'BCRA_262', 'Banco del Sol S.A.',                        31),
    ('BANK', 'BCRA_266', 'Nuevo Banco del Chaco S.A.',                32),
    ('BANK', 'BCRA_268', 'Bisel Bancos S.A.',                         33),
    ('BANK', 'BCRA_269', 'Banco Finansur S.A.',                       34),
    ('BANK', 'BCRA_277', 'Banco Pymenacion S.A.',                     35),
    ('BANK', 'BCRA_281', 'Banco Comafi S.A.',                         36),
    ('BANK', 'BCRA_285', 'Banco de Inversión y Comercio Exterior S.A.', 37),
    ('BANK', 'BCRA_295', 'Banco Piano S.A.',                          38),
    ('BANK', 'BCRA_301', 'Banco Julio S.A.',                          39),
    ('BANK', 'BCRA_305', 'Banco Meridian S.A.',                       40),
    ('BANK', 'BCRA_310', 'Banco Macro S.A.',                          41),
    ('BANK', 'BCRA_321', 'Nuevo Banco de Entre Ríos S.A.',            42),
    ('BANK', 'BCRA_322', 'Banco Ciudad S.A.',                         43),
    ('BANK', 'BCRA_330', 'Nuevo Banco de La Rioja S.A.',              44),
    ('BANK', 'BCRA_340', 'Banco Cencosud S.A.',                       45);

-- BANK_ACCOUNT_TYPE
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0007-0000-0000-000000000001', 'BANK_ACCOUNT_TYPE', 'CAJA_AHORRO',      'Caja de Ahorro',   'Savings Account',   'Conta Poupança',  1),
    ('00000200-0007-0000-0000-000000000002', 'BANK_ACCOUNT_TYPE', 'CUENTA_CORRIENTE', 'Cuenta Corriente', 'Checking Account',  'Conta Corrente',  2),
    ('00000200-0007-0000-0000-000000000003', 'BANK_ACCOUNT_TYPE', 'CUENTA_PLAZO',     'Plazo Fijo',       'Time Deposit',      'Depósito a Prazo',3),
    ('00000200-0007-0000-0000-000000000004', 'BANK_ACCOUNT_TYPE', 'VIRTUAL',          'Cuenta Virtual',   'Virtual Account',   'Conta Virtual',   4),
    ('00000200-0007-0000-0000-000000000005', 'BANK_ACCOUNT_TYPE', 'CRYPTO',           'Cripto',           'Crypto',            'Cripto',          5);

-- AUTHORITY_ROLE
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0008-0000-0000-000000000001', 'AUTHORITY_ROLE', 'PRESIDENTE',         'Presidente',           'President/CEO',     'Presidente',          1),
    ('00000200-0008-0000-0000-000000000002', 'AUTHORITY_ROLE', 'VICEPRESIDENTE',     'Vicepresidente',       'Vice President',    'Vice-Presidente',     2),
    ('00000200-0008-0000-0000-000000000003', 'AUTHORITY_ROLE', 'DIRECTOR',           'Director',             'Director',          'Diretor',             3),
    ('00000200-0008-0000-0000-000000000004', 'AUTHORITY_ROLE', 'DIRECTOR_SUPLENTE',  'Director Suplente',    'Alternate Director','Diretor Suplente',    4),
    ('00000200-0008-0000-0000-000000000005', 'AUTHORITY_ROLE', 'SINDICO_TITULAR',    'Síndico Titular',      'Statutory Auditor', 'Síndico Titular',     5),
    ('00000200-0008-0000-0000-000000000006', 'AUTHORITY_ROLE', 'SINDICO_SUPLENTE',   'Síndico Suplente',     'Alternate Auditor', 'Síndico Suplente',    6),
    ('00000200-0008-0000-0000-000000000007', 'AUTHORITY_ROLE', 'GERENTE',            'Gerente',              'Manager',           'Gerente',             7),
    ('00000200-0008-0000-0000-000000000008', 'AUTHORITY_ROLE', 'APODERADO',          'Apoderado',            'Attorney-in-Fact',  'Procurador',          8),
    ('00000200-0008-0000-0000-000000000009', 'AUTHORITY_ROLE', 'CONSEJO_ADMIN',      'Consejo Administración','Board of Directors','Conselho de Admin.',  9),
    ('00000200-0008-0000-0000-000000000010', 'AUTHORITY_ROLE', 'REVISOR_CUENTAS',    'Revisor de Cuentas',   'Accounts Reviewer', 'Revisor de Contas',  10);

-- SHARE_CLASS
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0009-0000-0000-000000000001', 'SHARE_CLASS', 'ORDINARIA',  'Ordinaria',  'Common',     'Ordinária',  1),
    ('00000200-0009-0000-0000-000000000002', 'SHARE_CLASS', 'PREFERIDA',  'Preferida',  'Preferred',  'Preferencial',2),
    ('00000200-0009-0000-0000-000000000003', 'SHARE_CLASS', 'CLASE_A',    'Clase A',    'Class A',    'Classe A',   3),
    ('00000200-0009-0000-0000-000000000004', 'SHARE_CLASS', 'CLASE_B',    'Clase B',    'Class B',    'Classe B',   4),
    ('00000200-0009-0000-0000-000000000005', 'SHARE_CLASS', 'CLASE_C',    'Clase C',    'Class C',    'Classe C',   5),
    ('00000200-0009-0000-0000-000000000006', 'SHARE_CLASS', 'CUOTA',      'Cuota SRL',  'SRL Quota',  'Cota SRL',   6);

-- RELATIONSHIP_TYPE
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0010-0000-0000-000000000001', 'RELATIONSHIP_TYPE', 'PARENT_COMPANY',  'Casa Matriz',      'Parent Company',    'Empresa Matriz',    1),
    ('00000200-0010-0000-0000-000000000002', 'RELATIONSHIP_TYPE', 'SUBSIDIARY',      'Subsidiaria',      'Subsidiary',        'Subsidiária',       2),
    ('00000200-0010-0000-0000-000000000003', 'RELATIONSHIP_TYPE', 'BRANCH',          'Sucursal',         'Branch',            'Filial',            3),
    ('00000200-0010-0000-0000-000000000004', 'RELATIONSHIP_TYPE', 'RELATED_PARTY',   'Parte Relacionada','Related Party',     'Parte Relacionada', 4),
    ('00000200-0010-0000-0000-000000000005', 'RELATIONSHIP_TYPE', 'GUARANTOR_FOR',   'Garante de',       'Guarantor for',     'Fiador de',         5),
    ('00000200-0010-0000-0000-000000000006', 'RELATIONSHIP_TYPE', 'APODERADO_DE',    'Apoderado de',     'Attorney for',      'Procurador de',     6),
    ('00000200-0010-0000-0000-000000000007', 'RELATIONSHIP_TYPE', 'GRUPO_ECONOMICO', 'Grupo Económico',  'Economic Group',    'Grupo Econômico',   7),
    ('00000200-0010-0000-0000-000000000008', 'RELATIONSHIP_TYPE', 'CONYUGE',         'Cónyuge',          'Spouse',            'Cônjuge',           8),
    ('00000200-0010-0000-0000-000000000009', 'RELATIONSHIP_TYPE', 'HIJO',            'Hijo/Hija',        'Child',             'Filho/Filha',       9),
    ('00000200-0010-0000-0000-000000000010', 'RELATIONSHIP_TYPE', 'PADRE',           'Padre/Madre',      'Parent',            'Pai/Mãe',          10),
    ('00000200-0010-0000-0000-000000000011', 'RELATIONSHIP_TYPE', 'HERMANO',         'Hermano/Hermana',  'Sibling',           'Irmão/Irmã',       11),
    ('00000200-0010-0000-0000-000000000012', 'RELATIONSHIP_TYPE', 'SOCIO',           'Socio',            'Partner/Shareholder','Sócio',           12);

-- TRADE_TYPE
INSERT INTO person.per_dataelement_tbl (id, category, code, label_es, label_en, label_pt, sort_order) VALUES
    ('00000200-0011-0000-0000-000000000001', 'TRADE_TYPE', 'INDIVIDUAL', 'Individual',  'Individual',  'Individual',  1),
    ('00000200-0011-0000-0000-000000000002', 'TRADE_TYPE', 'GROUP',      'Grupo',       'Group',       'Grupo',       2),
    ('00000200-0011-0000-0000-000000000003', 'TRADE_TYPE', 'FRANCHISE',  'Franquicia',  'Franchise',   'Franquia',    3),
    ('00000200-0011-0000-0000-000000000004', 'TRADE_TYPE', 'HOLDING',    'Holding',     'Holding',     'Holding',     4);
