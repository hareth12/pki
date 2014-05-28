pki
===

#### pkiservice-client/cli.bat usage:

```
Usage: <main class> [options]
  Options:
        --challenge     Challenge password (EJBCA entity password) (required
                        when enrolling to EJBCA)
  *     --cn            Subject CN to request
        --email         User email address to send confirmation emails to
                        (required when register)
  *     --mode          Mode (register/fetch/poll), poll only available for
                        OpenXPKI
        --password      One-time-password (required when poll/download from
                        OpenXPKI)
        --phone         User phone number to send PIN to (required when
                        register)
        --pkcs12-file   PKCS#12 output file
                        Default: cert.p12
  *     --provider      Provider (ejbca/openxpki)
```

#### authservice configuration:

* openxpkiServiceUrl - URL of OpenXPKI endpoint
* ejbcaRegistrationUrl - URL of EJBCA self-registration form
* mailContext - mail service JNDI URL
* datasourceContext - JDBC datasource JNDI (identity store)
* smsapiLogin - SMSAPI account login
* smsapiPassword - SMSAPI account password

#### pkiservice configuration:

* ejbcaRegistrationUrl - URL of EJBCA self-ragistrtion form
* ejbcaScepUrl - URL of EJBCA SCEP endpoint
* openxpkiScepUrl - URL of OpenXPKI SCEP endpoint
* datasourceContext - JDBC datasource JNDI (identity store)
* caIdentifier - identifier of CA to work against
* keySize - RSA key size

#### pkiservice-client configuration:

* authorizationServiceUrl - authorization service WSDL URL
* ejbcaServiceUrl - EJBCA service WSDL URL
* openxpkiServiceUrl - OpenXPKI service WSDL URL

#### example:

```
<authServiceUrl>http://vps59351.ovh.net:8080/authservice/AuthorizationService?wsdl</authServiceUrl>
<ejbcaServiceUrl>http://vps59351.ovh.net:8080/pkiservice/EJBCAService?wsdl</ejbcaServiceUrl>
<openxpkiServiceUrl>http://vps59351.ovh.net:8080/pkiservice/OpenXPKIService?wsdl</openxpkiServiceUrl>
```

```
cli.bat --mode register --provider ejbca --cn test --email tomeksamcik@tlen.pl --phone 501973096
#Approve self-registered user (Add End Entity)
cli.bat --mode fetch --provider ejbca --cn test --challenge twjPoVlB --pkcs12-file test.p12
openssl pkcs12 -in cert.p12 -info
```

```
cli.bat --mode register --provider openxpki --cn test --email tomeksamcik@tlen.pl --phone 501973096
#Approve Certificate Signing Request
cli.bat --mode poll --provider openxpki --cn test --password twjPoVlB
cli.bat --mode fetch --provider openxpki --cn test --password twjPoVlB --pkcs12-file test.p12
openssl pkcs12 -in test.p12 -info
```
