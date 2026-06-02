#!/usr/bin/env bash
# =============================================================================
# smoke_person_p3.sh — Person microservice Phase 3 smoke tests
# Contacts · BankAccounts · NaturalDetail · LegalDetail · IamLinks · Relationships
# =============================================================================
set -uo pipefail
# NOTE: -e intentionally omitted; test failures must not abort the script.

BASE="http://127.0.0.1:8099/imap/person"
IAM="http://127.0.0.1:8091/imap/iam"

# ── Secrets ───────────────────────────────────────────────────────────────────
source /opt/imap/person/.env
ADMIN_EMAIL="${SMOKE_ADMIN_EMAIL}"
ADMIN_PWD="${SMOKE_ADMIN_PWD}"
TENANT="${SMOKE_TENANT}"

PASS=0
FAIL=0

ok()  { echo "  PASS [$1]"; PASS=$((PASS+1)); }
fail(){ echo "  FAIL [$1] — $2"; FAIL=$((FAIL+1)); }

check_status() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then ok "$label"; else fail "$label" "expected HTTP $expected, got $actual"; fi
}

check_field() {
  local label="$1" field="$2" expected="$3" json="$4"
  local actual
  actual=$(echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('$field',''))" 2>/dev/null || echo "")
  if [ "$actual" = "$expected" ]; then ok "$label"; else fail "$label" "expected '$expected', got '$actual'"; fi
}

jq_get() {
  local field="$1" json="$2"
  echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('$field',''))" 2>/dev/null || echo ""
}

jq_len() {
  local json="$1"
  echo "$json" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0"
}

# ── Cleanup ───────────────────────────────────────────────────────────────────
# NOTE: PGPASSWORD must be exported — it cannot be inside a PG_CMD variable string
# because bash does not process VAR=value env-prefix from variable expansions.
export PGPASSWORD=07mePUkuzo
PG_CMD="psql -h 127.0.0.1 -U admindb -d iam -t -q"
IAM_ENTITY_SMOKE="aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
do_cleanup() {
  # Relationships: clean both sides to avoid FK blocks on person delete
  $PG_CMD -c "DELETE FROM person.per_relationship_tbl WHERE from_person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal')) OR to_person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal'));" 2>/dev/null || true
  # IAM links: clean by person + by known entity id (defensive)
  $PG_CMD -c "DELETE FROM person.per_iam_link_tbl WHERE iam_entity_id = '$IAM_ENTITY_SMOKE';" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_iam_link_tbl WHERE person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal'));" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_bank_account_tbl WHERE person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal'));" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_contact_tbl WHERE person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal'));" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_natural_detail_tbl WHERE person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal'));" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_legal_detail_tbl WHERE person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal'));" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_person_tbl WHERE legal_name IN ('Smoke P3 Natural','Smoke P3 Legal');" 2>/dev/null || true
}
do_cleanup

# ── JWT ───────────────────────────────────────────────────────────────────────
echo "=== AUTH ==="
LOGIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "$IAM/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PWD\"}")
LOGIN_STATUS=$(echo "$LOGIN_RESP" | tail -1)
LOGIN_BODY=$(echo "$LOGIN_RESP" | head -1)
check_status "login-200" "200" "$LOGIN_STATUS"
JWT=$(echo "$LOGIN_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])" 2>/dev/null || echo "")
if [ -n "$JWT" ]; then ok "jwt-obtained"; else fail "jwt-obtained" "empty JWT"; fi

AUTH="Authorization: Bearer $JWT"
TENANT_H="X-Tenant-Id: $TENANT"
FAKE_UUID="00000000-0000-0000-0000-000000000001"

# ── PA: Setup persons ─────────────────────────────────────────────────────────
echo ""
echo "=== PA — PERSONS SETUP ==="

PA1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"personType":"NATURAL","legalName":"Smoke P3 Natural"}')
PA1_STATUS=$(echo "$PA1_RESP" | tail -1)
PA1_BODY=$(echo "$PA1_RESP" | head -1)
check_status "PA1-natural-person-201" "201" "$PA1_STATUS"
P1=$(jq_get "id" "$PA1_BODY")
if [ -n "$P1" ]; then ok "PA1-person1-id-present"; else fail "PA1-person1-id-present" "empty id"; fi

PA2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"personType":"LEGAL","legalName":"Smoke P3 Legal"}')
PA2_STATUS=$(echo "$PA2_RESP" | tail -1)
PA2_BODY=$(echo "$PA2_RESP" | head -1)
check_status "PA2-legal-person-201" "201" "$PA2_STATUS"
P2=$(jq_get "id" "$PA2_BODY")
if [ -n "$P2" ]; then ok "PA2-person2-id-present"; else fail "PA2-person2-id-present" "empty id"; fi

# ── CT: Contacts ──────────────────────────────────────────────────────────────
echo ""
echo "=== CT — CONTACTS ==="

CT1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/contacts" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"value":"smoketest@example.com","primary":true}')
CT1_STATUS=$(echo "$CT1_RESP" | tail -1)
CT1_BODY=$(echo "$CT1_RESP" | head -1)
check_status "CT1-create-contact-201" "201" "$CT1_STATUS"
check_field "CT1-value" "value" "smoketest@example.com" "$CT1_BODY"
check_field "CT1-primary" "primary" "True" "$CT1_BODY"
check_field "CT1-validated" "validated" "False" "$CT1_BODY"

CT2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/contacts" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"value":"+54 9 351 555 1234","primary":false}')
CT2_STATUS=$(echo "$CT2_RESP" | tail -1)
check_status "CT2-create-contact2-201" "201" "$CT2_STATUS"

CT3_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$P1/contacts" \
  -H "$AUTH" -H "$TENANT_H")
CT3_STATUS=$(echo "$CT3_RESP" | tail -1)
CT3_BODY=$(echo "$CT3_RESP" | head -1)
check_status "CT3-list-200" "200" "$CT3_STATUS"
CT3_COUNT=$(jq_len "$CT3_BODY")
if [ "$CT3_COUNT" = "2" ]; then ok "CT3-count-2"; else fail "CT3-count-2" "expected 2, got $CT3_COUNT"; fi

CT4_RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/contacts" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"value":"x@x.com","primary":false}')
CT4_STATUS=$(echo "$CT4_RESP" | tail -1)
check_status "CT4-unknown-person-404" "404" "$CT4_STATUS"

CT5_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/contacts" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"value":"","primary":false}')
CT5_STATUS=$(echo "$CT5_RESP" | tail -1)
check_status "CT5-blank-value-400" "400" "$CT5_STATUS"

# ── BA: Bank Accounts ─────────────────────────────────────────────────────────
echo ""
echo "=== BA — BANK ACCOUNTS ==="

BA1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P2/bank-accounts" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"cbu":"0000003100057355010001","alias":"banco.smoke.test","primary":true,"validFrom":"2024-01-01"}')
BA1_STATUS=$(echo "$BA1_RESP" | tail -1)
BA1_BODY=$(echo "$BA1_RESP" | head -1)
check_status "BA1-create-account-201" "201" "$BA1_STATUS"
check_field "BA1-alias" "alias" "banco.smoke.test" "$BA1_BODY"
check_field "BA1-primary" "primary" "True" "$BA1_BODY"

BA2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P2/bank-accounts" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"cbu":"0720005888000012460076","alias":"banco.smoke.2","primary":false}')
BA2_STATUS=$(echo "$BA2_RESP" | tail -1)
check_status "BA2-create-account2-201" "201" "$BA2_STATUS"

BA3_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$P2/bank-accounts" \
  -H "$AUTH" -H "$TENANT_H")
BA3_STATUS=$(echo "$BA3_RESP" | tail -1)
BA3_BODY=$(echo "$BA3_RESP" | head -1)
check_status "BA3-list-200" "200" "$BA3_STATUS"
BA3_COUNT=$(jq_len "$BA3_BODY")
if [ "$BA3_COUNT" = "2" ]; then ok "BA3-count-2"; else fail "BA3-count-2" "expected 2, got $BA3_COUNT"; fi

BA4_RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/bank-accounts" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"alias":"x","primary":false}')
BA4_STATUS=$(echo "$BA4_RESP" | tail -1)
check_status "BA4-unknown-person-404" "404" "$BA4_STATUS"

# ── ND: NaturalDetail ─────────────────────────────────────────────────────────
echo ""
echo "=== ND — NATURAL DETAIL ==="

ND1_RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/v1/persons/$P1/natural-detail" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"birthDate":"1985-06-15","gender":"M","maritalStatus":"SOLTERO","nationalityCode":"ARG"}')
ND1_STATUS=$(echo "$ND1_RESP" | tail -1)
ND1_BODY=$(echo "$ND1_RESP" | head -1)
check_status "ND1-put-create-200" "200" "$ND1_STATUS"
check_field "ND1-gender" "gender" "M" "$ND1_BODY"
check_field "ND1-nationality" "nationalityCode" "ARG" "$ND1_BODY"

ND2_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$P1/natural-detail" \
  -H "$AUTH" -H "$TENANT_H")
ND2_STATUS=$(echo "$ND2_RESP" | tail -1)
ND2_BODY=$(echo "$ND2_RESP" | head -1)
check_status "ND2-get-200" "200" "$ND2_STATUS"
check_field "ND2-gender-persisted" "gender" "M" "$ND2_BODY"

ND3_RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/v1/persons/$P1/natural-detail" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"birthDate":"1985-06-15","gender":"F","maritalStatus":"CASADO","nationalityCode":"ARG"}')
ND3_STATUS=$(echo "$ND3_RESP" | tail -1)
ND3_BODY=$(echo "$ND3_RESP" | head -1)
check_status "ND3-put-update-200" "200" "$ND3_STATUS"
check_field "ND3-gender-updated" "gender" "F" "$ND3_BODY"

ND4_RESP=$(curl -s -w "\n%{http_code}" -X PUT \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/natural-detail" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"gender":"M"}')
ND4_STATUS=$(echo "$ND4_RESP" | tail -1)
check_status "ND4-unknown-person-404" "404" "$ND4_STATUS"

ND5_RESP=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/natural-detail" \
  -H "$AUTH" -H "$TENANT_H")
ND5_STATUS=$(echo "$ND5_RESP" | tail -1)
check_status "ND5-get-unknown-404" "404" "$ND5_STATUS"

# ── LD: LegalDetail ───────────────────────────────────────────────────────────
echo ""
echo "=== LD — LEGAL DETAIL ==="

LD1_RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/v1/persons/$P2/legal-detail" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"registrationNumber":"IGJ-12345","constitutionDate":"2010-03-15","registrationDate":"2010-06-01"}')
LD1_STATUS=$(echo "$LD1_RESP" | tail -1)
LD1_BODY=$(echo "$LD1_RESP" | head -1)
check_status "LD1-put-create-200" "200" "$LD1_STATUS"
check_field "LD1-regnumber" "registrationNumber" "IGJ-12345" "$LD1_BODY"

LD2_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$P2/legal-detail" \
  -H "$AUTH" -H "$TENANT_H")
LD2_STATUS=$(echo "$LD2_RESP" | tail -1)
LD2_BODY=$(echo "$LD2_RESP" | head -1)
check_status "LD2-get-200" "200" "$LD2_STATUS"
check_field "LD2-regnumber-persisted" "registrationNumber" "IGJ-12345" "$LD2_BODY"

LD3_RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/v1/persons/$P2/legal-detail" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"registrationNumber":"IGJ-99999","constitutionDate":"2010-03-15"}')
LD3_STATUS=$(echo "$LD3_RESP" | tail -1)
LD3_BODY=$(echo "$LD3_RESP" | head -1)
check_status "LD3-put-update-200" "200" "$LD3_STATUS"
check_field "LD3-regnumber-updated" "registrationNumber" "IGJ-99999" "$LD3_BODY"

LD4_RESP=$(curl -s -w "\n%{http_code}" -X PUT \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/legal-detail" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"registrationNumber":"X"}')
LD4_STATUS=$(echo "$LD4_RESP" | tail -1)
check_status "LD4-unknown-person-404" "404" "$LD4_STATUS"

# ── IL: IAM Links ─────────────────────────────────────────────────────────────
echo ""
echo "=== IL — IAM LINKS ==="

IAM_ENTITY_ID="$IAM_ENTITY_SMOKE"

IL1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P2/iam-links" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d "{\"iamEntityType\":\"USER\",\"iamEntityId\":\"$IAM_ENTITY_ID\",\"primary\":true}")
IL1_STATUS=$(echo "$IL1_RESP" | tail -1)
IL1_BODY=$(echo "$IL1_RESP" | head -1)
check_status "IL1-create-iamlink-201" "201" "$IL1_STATUS"
check_field "IL1-entitytype" "iamEntityType" "USER" "$IL1_BODY"
check_field "IL1-primary" "primary" "True" "$IL1_BODY"

IL2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P2/iam-links" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d "{\"iamEntityType\":\"USER\",\"iamEntityId\":\"$IAM_ENTITY_ID\",\"primary\":false}")
IL2_STATUS=$(echo "$IL2_RESP" | tail -1)
check_status "IL2-duplicate-409" "409" "$IL2_STATUS"

IL3_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$P2/iam-links" \
  -H "$AUTH" -H "$TENANT_H")
IL3_STATUS=$(echo "$IL3_RESP" | tail -1)
IL3_BODY=$(echo "$IL3_RESP" | head -1)
check_status "IL3-list-200" "200" "$IL3_STATUS"
IL3_COUNT=$(jq_len "$IL3_BODY")
if [ "$IL3_COUNT" = "1" ]; then ok "IL3-count-1"; else fail "IL3-count-1" "expected 1, got $IL3_COUNT"; fi

IL4_RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/iam-links" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"iamEntityType":"USER","iamEntityId":"11111111-1111-1111-1111-111111111111","primary":false}')
IL4_STATUS=$(echo "$IL4_RESP" | tail -1)
check_status "IL4-unknown-person-404" "404" "$IL4_STATUS"

IL5_RESP=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE/v1/persons/by-iam/USER/$IAM_ENTITY_ID" \
  -H "$AUTH" -H "$TENANT_H")
IL5_STATUS=$(echo "$IL5_RESP" | tail -1)
IL5_BODY=$(echo "$IL5_RESP" | head -1)
check_status "IL5-reverse-lookup-200" "200" "$IL5_STATUS"
check_field "IL5-legalname" "legalName" "Smoke P3 Legal" "$IL5_BODY"

IL6_RESP=$(curl -s -w "\n%{http_code}" -X GET \
  "$BASE/v1/persons/by-iam/USER/00000000-0000-0000-0000-000000000000" \
  -H "$AUTH" -H "$TENANT_H")
IL6_STATUS=$(echo "$IL6_RESP" | tail -1)
check_status "IL6-reverse-notfound-404" "404" "$IL6_STATUS"

# ── RL: Relationships ─────────────────────────────────────────────────────────
echo ""
echo "=== RL — RELATIONSHIPS ==="

REL_TYPE_ID="cccccccc-cccc-cccc-cccc-cccccccccccc"

RL1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/relationships" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d "{\"toPersonId\":\"$P2\",\"relTypeId\":\"$REL_TYPE_ID\",\"notes\":\"smoke test\",\"effectiveFrom\":\"2024-01-01\"}")
RL1_STATUS=$(echo "$RL1_RESP" | tail -1)
RL1_BODY=$(echo "$RL1_RESP" | head -1)
check_status "RL1-create-relationship-201" "201" "$RL1_STATUS"
RL1_ID=$(jq_get "id" "$RL1_BODY")
if [ -n "$RL1_ID" ]; then ok "RL1-rel-id-present"; else fail "RL1-rel-id-present" "empty id"; fi
check_field "RL1-notes" "notes" "smoke test" "$RL1_BODY"

RL2_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$P1/relationships" \
  -H "$AUTH" -H "$TENANT_H")
RL2_STATUS=$(echo "$RL2_RESP" | tail -1)
RL2_BODY=$(echo "$RL2_RESP" | head -1)
check_status "RL2-list-200" "200" "$RL2_STATUS"
RL2_COUNT=$(jq_len "$RL2_BODY")
if [ "$RL2_COUNT" = "1" ]; then ok "RL2-count-1"; else fail "RL2-count-1" "expected 1, got $RL2_COUNT"; fi

RL3_RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/relationships" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d "{\"toPersonId\":\"$P2\",\"relTypeId\":\"$REL_TYPE_ID\"}")
RL3_STATUS=$(echo "$RL3_RESP" | tail -1)
check_status "RL3-unknown-fromperson-404" "404" "$RL3_STATUS"

RL4_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/relationships" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d "{\"toPersonId\":\"00000000-0000-0000-0000-000000000000\",\"relTypeId\":\"$REL_TYPE_ID\"}")
RL4_STATUS=$(echo "$RL4_RESP" | tail -1)
check_status "RL4-unknown-toperson-404" "404" "$RL4_STATUS"

# ── Cleanup ───────────────────────────────────────────────────────────────────
echo ""
echo "=== CLEANUP ==="
do_cleanup
ok "cleanup"

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
echo "=============================="
echo " PASS: $PASS  FAIL: $FAIL"
echo " TOTAL: $((PASS+FAIL))"
echo "=============================="
if [ "$FAIL" = "0" ]; then exit 0; else exit 1; fi
