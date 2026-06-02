#!/usr/bin/env bash
# =============================================================================
# smoke_person_p2.sh — Person microservice Phase 2 smoke tests
# TaxId + FiscalProfile CRUD + by-cuit lookup
# =============================================================================
set -uo pipefail
# NOTE: -e intentionally omitted; test failures must not abort the script.

BASE="http://127.0.0.1:8099/imap/person"
IAM="http://127.0.0.1:8091/imap/iam"

# ── Load secrets from env file ────────────────────────────────────────────────
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

# safe extraction of a JSON field — returns empty string on any error
jq_get() {
  local field="$1" json="$2"
  echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('$field',''))" 2>/dev/null || echo ""
}

# safe extraction of array length
jq_len() {
  local json="$1"
  echo "$json" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0"
}

# ── Cleanup helper ────────────────────────────────────────────────────────────
# NOTE: PGPASSWORD must be exported — VAR=value inside a string variable is not processed as env-prefix.
export PGPASSWORD=07mePUkuzo
PG_CMD="psql -h 127.0.0.1 -U admindb -d iam -t -q"
do_cleanup() {
  # Run each statement in its own connection to avoid single-TX rollback on FK error.
  $PG_CMD -c "DELETE FROM person.per_fiscal_profile_tbl WHERE person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Acme SA (smoke-p2)','Juan P (smoke-p2)'));" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_tax_id_tbl WHERE tax_id_value IN ('30-12345678-9','20-98765432-1');" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_person_tbl WHERE legal_name IN ('Acme SA (smoke-p2)','Juan P (smoke-p2)');" 2>/dev/null || true
}
do_cleanup

# ── Obtain JWT ────────────────────────────────────────────────────────────────
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

# ── PA: Person setup ──────────────────────────────────────────────────────────
echo ""
echo "=== PA — PERSON SETUP ==="
PA1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"personType":"LEGAL","legalName":"Acme SA (smoke-p2)","tradeName":"Acme"}')
PA1_STATUS=$(echo "$PA1_RESP" | tail -1)
PA1_BODY=$(echo "$PA1_RESP" | head -1)
check_status "PA1-create-person-201" "201" "$PA1_STATUS"
PERSON_ID=$(jq_get "id" "$PA1_BODY")
if [ -n "$PERSON_ID" ]; then ok "PA1-person-id-present"; else fail "PA1-person-id-present" "empty id"; fi

PA2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"personType":"NATURAL","legalName":"Juan P (smoke-p2)"}')
PA2_STATUS=$(echo "$PA2_RESP" | tail -1)
PA2_BODY=$(echo "$PA2_RESP" | head -1)
check_status "PA2-create-natural-person-201" "201" "$PA2_STATUS"
PERSON2_ID=$(jq_get "id" "$PA2_BODY")

# ── TB: TaxId CRUD ────────────────────────────────────────────────────────────
echo ""
echo "=== TB — TAX ID ==="

# TB1: Add CUIT to legal person
TB1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$PERSON_ID/tax-ids" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"documentTypeKey":"CUIT","taxIdValue":"30-12345678-9","validFrom":"2020-01-01"}')
TB1_STATUS=$(echo "$TB1_RESP" | tail -1)
TB1_BODY=$(echo "$TB1_RESP" | head -1)
check_status "TB1-add-cuit-201" "201" "$TB1_STATUS"
check_field "TB1-cuit-value" "taxIdValue" "30-12345678-9" "$TB1_BODY"
check_field "TB1-cuit-current" "current" "True" "$TB1_BODY"
TAX_ID=$(jq_get "id" "$TB1_BODY")

# TB2: Add CUIL to natural person
TB2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$PERSON2_ID/tax-ids" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"documentTypeKey":"CUIL","taxIdValue":"20-98765432-1"}')
TB2_STATUS=$(echo "$TB2_RESP" | tail -1)
check_status "TB2-add-cuil-201" "201" "$TB2_STATUS"

# TB3: Conflict — duplicate CUIT same value → 409
TB3_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$PERSON_ID/tax-ids" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"documentTypeKey":"CUIT","taxIdValue":"30-12345678-9"}')
TB3_STATUS=$(echo "$TB3_RESP" | tail -1)
check_status "TB3-duplicate-409" "409" "$TB3_STATUS"

# TB4: Unknown document type key → 422
TB4_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$PERSON_ID/tax-ids" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"documentTypeKey":"UNKNOWN_TYPE","taxIdValue":"99-99999999-9"}')
TB4_STATUS=$(echo "$TB4_RESP" | tail -1)
check_status "TB4-unknown-doctype-422" "422" "$TB4_STATUS"

# TB5: List tax IDs for person → 1 entry
TB5_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$PERSON_ID/tax-ids" \
  -H "$AUTH" -H "$TENANT_H")
TB5_STATUS=$(echo "$TB5_RESP" | tail -1)
TB5_BODY=$(echo "$TB5_RESP" | head -1)
check_status "TB5-list-taxids-200" "200" "$TB5_STATUS"
TB5_COUNT=$(jq_len "$TB5_BODY")
if [ "$TB5_COUNT" = "1" ]; then ok "TB5-taxid-count-1"; else fail "TB5-taxid-count-1" "expected 1, got $TB5_COUNT"; fi

# ── TC: by-cuit lookup ────────────────────────────────────────────────────────
echo ""
echo "=== TC — BY-CUIT LOOKUP ==="

# TC1: Find person by existing CUIT → PersonDto with correct legalName
TC1_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/by-cuit/30-12345678-9" \
  -H "$AUTH" -H "$TENANT_H")
TC1_STATUS=$(echo "$TC1_RESP" | tail -1)
TC1_BODY=$(echo "$TC1_RESP" | head -1)
check_status "TC1-by-cuit-200" "200" "$TC1_STATUS"
check_field "TC1-legal-name" "legalName" "Acme SA (smoke-p2)" "$TC1_BODY"

# TC2: Non-existent CUIT → 404
TC2_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/by-cuit/00-00000000-0" \
  -H "$AUTH" -H "$TENANT_H")
TC2_STATUS=$(echo "$TC2_RESP" | tail -1)
check_status "TC2-not-found-404" "404" "$TC2_STATUS"

# ── FP: FiscalProfile CRUD ────────────────────────────────────────────────────
echo ""
echo "=== FP — FISCAL PROFILE ==="

# FP1: Add ARCA Responsable Inscripto
FP1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$PERSON_ID/fiscal-profiles" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"organismCode":"ARCA","fiscalPosition":"RI","registeredSince":"2015-01-01"}')
FP1_STATUS=$(echo "$FP1_RESP" | tail -1)
FP1_BODY=$(echo "$FP1_RESP" | head -1)
check_status "FP1-add-arca-ri-201" "201" "$FP1_STATUS"
check_field "FP1-organism" "organismCode" "ARCA" "$FP1_BODY"
check_field "FP1-position" "fiscalPosition" "RI" "$FP1_BODY"
check_field "FP1-active" "active" "True" "$FP1_BODY"

# FP2: Add IIBB_CBA profile
FP2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$PERSON_ID/fiscal-profiles" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"organismCode":"IIBB_CBA","fiscalPosition":"MT","registeredSince":"2018-01-01"}')
FP2_STATUS=$(echo "$FP2_RESP" | tail -1)
check_status "FP2-add-iibb-201" "201" "$FP2_STATUS"

# FP3: List fiscal profiles → 2 entries
FP3_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$PERSON_ID/fiscal-profiles" \
  -H "$AUTH" -H "$TENANT_H")
FP3_STATUS=$(echo "$FP3_RESP" | tail -1)
FP3_BODY=$(echo "$FP3_RESP" | head -1)
check_status "FP3-list-profiles-200" "200" "$FP3_STATUS"
FP3_COUNT=$(jq_len "$FP3_BODY")
if [ "$FP3_COUNT" = "2" ]; then ok "FP3-profile-count-2"; else fail "FP3-profile-count-2" "expected 2, got $FP3_COUNT"; fi

# FP4: Person not found → 404
FP4_RESP=$(curl -s -w "\n%{http_code}" -X POST \
  "$BASE/v1/persons/00000000-0000-0000-0000-000000000000/fiscal-profiles" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"organismCode":"ARCA","fiscalPosition":"CF"}')
FP4_STATUS=$(echo "$FP4_RESP" | tail -1)
check_status "FP4-person-not-found-404" "404" "$FP4_STATUS"

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
