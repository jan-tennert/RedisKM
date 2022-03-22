# RedisKM

A Kotlin Multiplatform [Redis](https://redis.io/) Client. \
Also supports RedisJSON with built-in KotlinX Serialization support.

Version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.jan-tennert.rediskm/RedisKM)](https://search.maven.org/artifact/io.github.jan-tennert.rediskm/RedisKM)

Available for: 

![JVM](https://img.shields.io/badge/-jvm-brightgreen)
- android (not tested)
- jvm

![JS](https://img.shields.io/badge/-js-ffd900) 
- browser
- nodejs 

![Native](https://img.shields.io/badge/-native-blue)
- mingwx64
- linuxX64
- macosX64
- macosArm64

**secure connections are only supported on mingx64, JVM and JS**

# Introduction

You can build a new connection very easily with RedisClient:

```kotlin
val client = RedisClient(host = "URL", port = port, password = "password", user = "user")
client.connect()

//then set a value and get it back
client.put("login", "redis", expirationDuration = 10.hours)
val login = client.get<String>("login")

//for more options on a specific element:
val login = client.get<RedisElement>("login")
//to call things like
login.persist()
login.expire(10.hours)
login.rename("newLogin")
```

More information about Lists, Sets, the JSON module and more can be found in the [Wiki]()

# Installation

```kotlin
dependencies {
    implementation("io.github.jan-tennert.rediskm:RedisKM:VERSION")
}
```

If you want a specific target you can change the artifact id to RedisKM-[target]

