# NabSuite

**Builds**: [https://ci.froobworld.com/job/NabSuite/](https://ci.froobworld.com/job/NabSuite/)

## About
NabSuite is a Paper plugin used by FroobWorld to provide essential features, including: teleportation, area claiming, chest locking, ban management and more.

## Building

1. Install dependency NabConfiguration to maven local
```bash
git clone https://github.com/froobynooby/nab-configuration
cd nab-configuration
./gradlew clean install
```
2. Clone NabSuite and build
```bash
git clone https://github.com/FroobWorld/NabSuite
cd NabSuite
./gradlew clean build
```

3. Find jar in `NabSuite/build/libs`
