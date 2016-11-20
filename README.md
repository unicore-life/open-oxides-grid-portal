# oxides-grid-portal

[![Build Status](https://travis-ci.org/unicore-life/oxides-grid-portal.svg?branch=master)](https://travis-ci.org/unicore-life/oxides-grid-portal)

[![Stories in Ready](https://badge.waffle.io/unicore-life/oxides-grid-portal.png?label=ready&title=Ready)](https://waffle.io/unicore-life/oxides-grid-portal)


## Running service on production environment:

Enter service directory (because of `identityConfig` property) and run start script:

```bash
cd oxides-grid-portal-*
./bin/oxides-grid-portal
```

## Configuration of Unity IDM

Oxides Grid Portal uses Unity IDM for authentication. After setting up domain name for 
the portal and certificate, it may be required to put lines presented below to
SAML web identity provider endpoint configuration.

```
unity.saml.acceptedSP.OXIDES.dn=CN=oxides.grid.portal,O=GRID,C=PL
unity.saml.acceptedSP.OXIDES.returnURL=https://oxides.grid.portal/authn/sign-in
unity.saml.acceptedSP.OXIDES.certificate=OXIDES-PORTAL-CERT
unity.saml.acceptedSP.OXIDES.postLogoutEndpoint=https://oxides.grid.portal/authn/sign-out
```

## Development

### Building

Just clone the project and run Gradle command presented below.

```bash
./gradlew build
```

### Releasing

To see current version of the sources use Gradle task
[currentVersion](http://axion-release-plugin.readthedocs.io/en/latest/configuration/tasks.html#currentversion)
(it is stored as a git tag).

```bash
./gradlew currentVersion
```

To release a new version use
[release](http://axion-release-plugin.readthedocs.io/en/latest/configuration/tasks.html#release) task.
Later, for uploading artifact to [Bintray](https://dl.bintray.com/unicore-life/maven) maven repository
use [bintrayUpload](https://github.com/novoda/bintray-release) task.
Sample command are presented below.

```
./gradlew release
./gradlew bintrayUpload -PdryRun=false
```

Remember to configure [Bintray](https://bintray.com) user and key by using parameters
`-PbintrayUser=BINTRAY_USERNAME -PbintrayKey=BINTRAY_KEY` or just put them in `gradle.properties` file.


# Links

* [UNICORE](http://unicore.eu)
* [Unity IDM](http://unity-idm.eu)

## Other links which may come handy
 
* [websockets](http://g00glen00b.be/spring-websockets-config/)
* https://github.com/vdenotaris/spring-boot-security-saml-sample
* https://github.com/spring-projects/spring-boot/tree/master/spring-boot-samples
