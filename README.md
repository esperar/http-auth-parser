# http-auth-parser

[![Javadocs](https://www.javadoc.io/badge/im.toss/http-auth-parser.svg)](https://www.javadoc.io/doc/im.toss/http-auth-parser)
[![Build Status](https://travis-ci.org/toss/http-auth-parser.svg?branch=master)](https://travis-ci.org/toss/http-auth-parser)
[![codecov](https://codecov.io/gh/toss/http-auth-parser/branch/master/graph/badge.svg)](https://codecov.io/gh/toss/http-auth-parser)

HTTP Authorization header parser

## Usage

To add a dependency on http-auth-parser using Gradle:

    compile "im.toss:http-auth-parser:0.1.2"

For more about depending on http-auth-parser, see [the central repository](https://search.maven.org/#artifactdetails%7Cim.toss%7Chttp-auth-parser%7C0.1.2%7Cjar).

### Examples

parsing scheme and token68:

```java
credentials = HttpAuthCredentials.parse("Basic YWxhZGRpbjpvcGVuc2VzYW1l");
credentials.getScheme().equals("Basic");
credentials.getToken().equals("YWxhZGRpbjpvcGVuc2VzYW1l");
```

parsing scheme and auth params:

```java
credentials = HttpAuthCredentials.parse("Custom k1=v1, k2=v2");
credentials.getScheme().equals("Custom");
credentials.getToken().equals("");
credentials.getParams().get("k1").equals(Arrays.asList("v1"));
credentials.getParams().get("k2").equals(Arrays.asList("v1"));
```

parsing scheme, token68 and auth params even if it violates RFC 7235:

```java
credentials = HttpAuthCredentials.parse("Custom mytoken, k1=v1");
credentials.getScheme().equals("Custom");
credentials.getToken().equals("mytoken");
credentials.getParams().get("k1").equals(Arrays.asList("v1"));
```

parsing auth params with multiple parameter names even if it violates RFC 7235:

```java
credentials = HttpAuthCredentials.parse("Custom k1=v1, k1=v2");
credentials.getParams().get("k1").equals(Arrays.asList("v1", "v2"));
```

## Maintainers

* [Yi EungJun](https://github.com/eungjun-yi)

## License

    Copyright 2018 Viva Republica, Inc.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
