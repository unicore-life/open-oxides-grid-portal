#!/usr/bin/env bash

# Create self-signed certificate for tests:
#
keytool -genkey \
    -keystore oxides-portal-test-identity.jks \
    -storepass oxides \
    -keypass oxides \
    -keyalg RSA \
    -dname "CN=oxides-portal-test,O=unicore-life,C=PL" \
    -alias oxides-portal-test \
    -validity 3600
