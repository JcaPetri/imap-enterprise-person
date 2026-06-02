#!/usr/bin/env bash
# =============================================================================
# smoke_person_p4.sh — Person microservice Phase 4 smoke tests
# Addresses (PostGIS geometry(POINT,4326))
# =============================================================================
set -uo pipefail

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

check_not_empty() {
  local label="$1" field="$2" json="$3"
  local actual
  actual=$(echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); v=d.get('$field'); print('' if v is None else v)" 2>/dev/null || echo "")
  if [ -n "$actual" ] && [ "$actual" != "None" ]; then ok "$label"; else fail "$label" "field '$field' is empty/null"; fi
}

jq_get() {
  local field="$1" json="$2"
  echo "$json" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('$field',''))" 2>/dev/null || echo ""
}

jq_len() {
  local json="$1"
  echo "$json" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0"
}

jq_arr_field() {
  local index="$1" field="$2" json="$3"
  echo "$json" | python3 -c "import sys,json; arr=json.load(sys.stdin); print(arr[$index].get('$field','') if $index < len(arr) else '')" 2>/dev/null || echo ""
}

# ── Cleanup ───────────────────────────────────────────────────────────────────
export PGPASSWORD=07mePUkuzo
PG_CMD="psql -h 127.0.0.1 -U admindb -d iam -t -q"

do_cleanup() {
  $PG_CMD -c "DELETE FROM person.per_address_tbl WHERE person_id IN (SELECT id FROM person.per_person_tbl WHERE legal_name IN ('Smoke P4 Addr'));" 2>/dev/null || true
  $PG_CMD -c "DELETE FROM person.per_person_tbl WHERE legal_name IN ('Smoke P4 Addr');" 2>/dev/null || true
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

# ── PA: Setup person ──────────────────────────────────────────────────────────
echo ""
echo "=== PA — PERSON SETUP ==="

PA_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"personType":"LEGAL","legalName":"Smoke P4 Addr"}')
PA_STATUS=$(echo "$PA_RESP" | tail -1)
PA_BODY=$(echo "$PA_RESP" | head -1)
check_status "PA1-person-201" "201" "$PA_STATUS"
P1=$(jq_get "id" "$PA_BODY")
if [ -n "$P1" ]; then ok "PA1-person-id-present"; else fail "PA1-person-id-present" "empty id"; fi

# ── AD: Addresses ─────────────────────────────────────────────────────────────
echo ""
echo "=== AD — ADDRESSES ==="

# AD1: address without coordinates
AD1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/addresses" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{
    "street": "Av. Corrientes",
    "streetNumber": "1234",
    "floor": "3",
    "apartment": "B",
    "city": "Buenos Aires",
    "postalCode": "C1043AAZ"
  }')
AD1_STATUS=$(echo "$AD1_RESP" | tail -1)
AD1_BODY=$(echo "$AD1_RESP" | head -1)
check_status "AD1-no-coords-201" "201" "$AD1_STATUS"
AD1_ID=$(jq_get "id" "$AD1_BODY")
if [ -n "$AD1_ID" ]; then ok "AD1-id-present"; else fail "AD1-id-present" "empty id"; fi
check_field "AD1-street" "street" "Av. Corrientes" "$AD1_BODY"
check_field "AD1-city" "city" "Buenos Aires" "$AD1_BODY"
# No location: latitude/longitude should be null/empty
LAT1=$(echo "$AD1_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); print('' if d.get('latitude') is None else d.get('latitude'))" 2>/dev/null || echo "")
if [ -z "$LAT1" ] || [ "$LAT1" = "None" ]; then ok "AD1-latitude-null"; else fail "AD1-latitude-null" "expected null, got $LAT1"; fi

# AD2: address WITH coordinates (Plaza de Mayo, Buenos Aires)
AD2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/addresses" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{
    "street": "Hipolito Yrigoyen",
    "streetNumber": "250",
    "city": "Buenos Aires",
    "postalCode": "C1086AAB",
    "latitude": -34.6083,
    "longitude": -58.3712
  }')
AD2_STATUS=$(echo "$AD2_RESP" | tail -1)
AD2_BODY=$(echo "$AD2_RESP" | head -1)
check_status "AD2-with-coords-201" "201" "$AD2_STATUS"
AD2_ID=$(jq_get "id" "$AD2_BODY")
if [ -n "$AD2_ID" ]; then ok "AD2-id-present"; else fail "AD2-id-present" "empty id"; fi
check_field "AD2-city" "city" "Buenos Aires" "$AD2_BODY"
# Verify coordinates roundtrip
LAT2=$(echo "$AD2_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); v=d.get('latitude'); print('' if v is None else round(float(v),4))" 2>/dev/null || echo "")
LNG2=$(echo "$AD2_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); v=d.get('longitude'); print('' if v is None else round(float(v),4))" 2>/dev/null || echo "")
if [ "$LAT2" = "-34.6083" ]; then ok "AD2-latitude-roundtrip"; else fail "AD2-latitude-roundtrip" "expected -34.6083, got $LAT2"; fi
if [ "$LNG2" = "-58.3712" ]; then ok "AD2-longitude-roundtrip"; else fail "AD2-longitude-roundtrip" "expected -58.3712, got $LNG2"; fi

# ── AL: List addresses ────────────────────────────────────────────────────────
echo ""
echo "=== AL — LIST ADDRESSES ==="

AL_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/v1/persons/$P1/addresses" \
  -H "$AUTH" -H "$TENANT_H")
AL_STATUS=$(echo "$AL_RESP" | tail -1)
AL_BODY=$(echo "$AL_RESP" | head -1)
check_status "AL1-list-200" "200" "$AL_STATUS"
AL_LEN=$(jq_len "$AL_BODY")
if [ "$AL_LEN" = "2" ]; then ok "AL1-list-2-items"; else fail "AL1-list-2-items" "expected 2, got $AL_LEN"; fi

# ── AE: Edge cases ────────────────────────────────────────────────────────────
echo ""
echo "=== AE — EDGE CASES ==="

# AE1: only lat, no lng → no location stored (not an error, just no point)
AE1_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/$P1/addresses" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"city":"Rosario","latitude":-32.9468}')
AE1_STATUS=$(echo "$AE1_RESP" | tail -1)
AE1_BODY=$(echo "$AE1_RESP" | head -1)
check_status "AE1-partial-coords-201" "201" "$AE1_STATUS"
LAT_AE1=$(echo "$AE1_BODY" | python3 -c "import sys,json; d=json.load(sys.stdin); v=d.get('latitude'); print('' if v is None else v)" 2>/dev/null || echo "")
if [ -z "$LAT_AE1" ] || [ "$LAT_AE1" = "None" ]; then ok "AE1-partial-lat-not-stored"; else fail "AE1-partial-lat-not-stored" "expected null without lng, got $LAT_AE1"; fi

# AE2: unknown person → 404
AE2_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/v1/persons/00000000-0000-0000-0000-000000000099/addresses" \
  -H "Content-Type: application/json" -H "$AUTH" -H "$TENANT_H" \
  -d '{"city":"Nowhere"}')
AE2_STATUS=$(echo "$AE2_RESP" | tail -1)
check_status "AE2-unknown-person-404" "404" "$AE2_STATUS"

# ── Cleanup ───────────────────────────────────────────────────────────────────
do_cleanup

# ── Summary ───────────────────────────────────────────────────────────────────
echo ""
echo "============================================"
echo "  Person Phase 4 — Addresses"
echo "  PASS: $PASS   FAIL: $FAIL"
if [ "$FAIL" = "0" ]; then
  echo "  ALL PASS ($(( PASS )) checks)"
  echo "============================================"
  exit 0
else
  echo "  FAILURES: $FAIL"
  echo "============================================"
  exit 1
fi
