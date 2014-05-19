pki
===

#### pkiservice-client/cli.bat usage:

```
Usage: <main class> [options]
  Options:
        --challenge     Challenge password (EJBCA entity password) (required
                        when enroll)
  *     --cn            Subject CN to request
        --email         User email address to send confirmation emails to
                        (required when register)
  *     --mode          Mode (register/enroll)
        --phone         User phone number to send PIN to (required when
                        register)
        --pkcs12-file   PKCS#12 output file
                        Default: cert.p12
```

#### pkiservice configuration:

* ejbcaRegistrationUrl - URL of EJBCA self-ragistrtion form
* ejbcaScepUrl - URL of EJBCA SCEP endpoint
* mailContext - mail service JNDI URL
* datasourceContext - JDBC datasource JNDI (identity store)
* smsapiLogin - SMSAPI account login
* smsapiPassword - SMSAPI account password
* caIdentifier - identifier of CA to work against
* keySize - RSA key size

#### pkiservice-client configuration:

* authorizationServiceUrl - authorization service WSDL URL
* ejbcaServiceUrl - EJBCA service WSDL URL

#### example:

```
<authorizationServiceUrl>https://vps59351.ovh.net:8443/pkiservice/AuthorizationService?wsdl</authorizationServiceUrl>
<ejbcaServiceUrl>https://vps59351.ovh.net:8443/pkiservice/EJBCAService?wsdl</ejbcaServiceUrl>
```

```
cli.bat --mode "register" --cn "test" --email "tomeksamcik@tlen.pl" --phone 501973096
cli.bat --mode "enroll" --cn "test" --challenge "twjPoVlB" --pkcs12-file "test.p12"
openssl pkcs12 -in cert.p12 -info
```
