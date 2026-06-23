#!/bin/bash

# FinTrack Backend - Dummy Data Seed Script
#
# Kullanım:
#   ./seed-data.sh
#
# Ne yapar:
#   1. Sabit bir demo kullanıcı ile register/login olur
#      (zaten kayıtlıysa otomatik login'e düşer)
#   2. O kullanıcı adına birkaç gerçekçi transaction ekler
#      (tarihler GÜNCEL AYA göre ayarlanmıştır, "Bu Ay" özetinde görünür)
#   3. O kullanıcı adına birkaç bütçe ekler
#
# Backend'in http://localhost:8080 adresinde ÇALIŞIYOR olması gerekir:
#   ./gradlew :backend:run
#
# NOT: H2 in-memory veritabanı kullanıldığı için, backend her yeniden
# başlatıldığında veri sıfırlanır — bu scripti her seferinde tekrar
# çalıştırman gerekir.

set -e

BASE_URL="http://localhost:8080"
EMAIL="seed@fintrack.com"
PASSWORD="seed1234"
FULL_NAME="Seed User"

echo "== FinTrack Dummy Data Seed =="
echo ""

# --- 0. Tarih hesaplamaları (şu anki aya göre, milisaniye) ---
# NOT: macOS'un (BSD) date komutu %N'i desteklemiyor, bu yüzden
# saniye cinsinden alıp *1000 ile milisaniyeye çeviriyoruz.
NOW_MS=$(($(date +%s) * 1000))
DAY_MS=86400000

ago() {
  # $1 = kaç gün önce (negatif verirsen ileri tarih olur)
  echo $((NOW_MS - $1 * DAY_MS))
}

# --- 1. Kullanıcıyı oluştur (zaten varsa register 409 döner, login'e düşeriz) ---
echo "[1/3] Kullanici kaydediliyor (varsa atlanacak)..."

REGISTER_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"fullName\":\"$FULL_NAME\"}")

HTTP_CODE=$(echo "$REGISTER_RESPONSE" | tail -n1)
BODY=$(echo "$REGISTER_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ]; then
  echo "    Yeni kullanici olusturuldu: $EMAIL"
  TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//')
else
  echo "    Kullanici zaten mevcut, login yapiliyor..."
  LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
  TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | sed 's/"token":"//;s/"//')
fi

if [ -z "$TOKEN" ]; then
  echo "HATA: Token alinamadi. Backend calisiyor mu? ($BASE_URL)"
  exit 1
fi

echo "    Token alindi."
echo ""

# --- 2. Dummy transaction'lar ---
# NOT: Kategori adlari Android'in domain/Category enum'una uymak ZORUNDA:
# FOOD, TRANSPORT, SHOPPING, BILLS, HEALTH, ENTERTAINMENT, EDUCATION,
# OTHER_EXPENSE, SALARY, FREELANCE, INVESTMENT, OTHER_INCOME
echo "[2/3] Dummy transaction'lar ekleniyor (guncel ay icinde)..."

add_transaction() {
  local amount=$1
  local type=$2
  local category=$3
  local description=$4
  local date=$5

  curl -s -o /dev/null -w "    %{http_code} - $description\n" -X POST "$BASE_URL/transactions" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"amount\":$amount,\"type\":\"$type\",\"category\":\"$category\",\"description\":\"$description\",\"date\":$date,\"accountId\":\"acc-demo\"}"
}

# Gelirler
add_transaction 25000.0 "INCOME" "SALARY" "Maas" "$(ago 20)"
add_transaction 1500.0 "INCOME" "FREELANCE" "Freelance is" "$(ago 15)"

# Giderler
add_transaction 350.0 "EXPENSE" "FOOD" "Market alisverisi" "$(ago 12)"
add_transaction 120.0 "EXPENSE" "FOOD" "Restoran" "$(ago 10)"
add_transaction 800.0 "EXPENSE" "BILLS" "Kira" "$(ago 18)"
add_transaction 200.0 "EXPENSE" "TRANSPORT" "Yakit" "$(ago 8)"
add_transaction 89.0 "EXPENSE" "ENTERTAINMENT" "Sinema" "$(ago 6)"
add_transaction 450.0 "EXPENSE" "SHOPPING" "Giyim" "$(ago 4)"
add_transaction 60.0 "EXPENSE" "BILLS" "Elektrik faturasi" "$(ago 2)"

echo ""

# --- 3. Dummy budget'lar ---
echo "[3/3] Dummy budget'lar ekleniyor..."

add_budget() {
  local name=$1
  local limit=$2
  local category=$3
  local period=$4
  local startDate=$5
  local endDate=$6

  curl -s -o /dev/null -w "    %{http_code} - $name\n" -X POST "$BASE_URL/budgets" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"$name\",\"limit\":$limit,\"category\":\"$category\",\"period\":\"$period\",\"startDate\":$startDate,\"endDate\":$endDate}"
}

MONTH_START=$(ago 25)
MONTH_END=$(ago -10)

add_budget "Yemek Butcesi" 1000.0 "FOOD" "MONTHLY" "$MONTH_START" "$MONTH_END"
add_budget "Ulasim Butcesi" 500.0 "TRANSPORT" "MONTHLY" "$MONTH_START" "$MONTH_END"
add_budget "Eglence Butcesi" 300.0 "ENTERTAINMENT" "MONTHLY" "$MONTH_START" "$MONTH_END"

echo ""
echo "== Tamamlandi =="
echo ""
echo "Demo kullanici bilgileri (Android uygulamasinda bununla giris yapabilirsin):"
echo "  E-posta : $EMAIL"
echo "  Sifre   : $PASSWORD"
echo ""
echo "NOT: Backend H2 in-memory kullaniyor — her './gradlew :backend:run'"
echo "yeniden baslatildiginda veri sifirlanir, bu scripti tekrar calistir."