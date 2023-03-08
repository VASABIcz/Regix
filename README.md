# Simple regular expression parser

### html request parsing
```kt
    val n = "\\\n"
    val field = "[(^[:]+): (^[$n]+)$n]"
    val methods = "[GET]|[POST]|[PATCH]|[PUT]|[OPTIONS]|[DELETE]|[HEAD]|[CONNECT]"
    val httpVer = "HTTP/1\\.1"
    val a = "[($methods) (^[ ]+) $httpVer$n$field+]"

    val httpReq =
        "GET /api/doma.php?pepik=123 HTTP/1.1\n" +
                "Host: 127.0.0.1:8080\n" +
                "User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/110.0\n" +
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8\n" +
                "Accept-Language: en-US,en;q=0.5\n" +
                "Accept-Encoding: gzip, deflate, br\n" +
                "DNT: 1\n" +
                "Connection: keep-alive\n" +
                "Cookie: JSESSIONID=0DF49810BD55112FA90891FE5E687394\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "Sec-Fetch-Dest: document\n" +
                "Sec-Fetch-Mode: navigate\n" +
                "Sec-Fetch-Site: none\n" +
                "Sec-Fetch-User: ?1\n" +
                "Sec-GPC: 1\n" +
                "Pragma: no-cache\n" +
                "Cache-Control: no-cache"

    val cs = mutableListOf<MutableList<String>>()
    val regix = constructRegix2(Lexer(a))

    regix.first().print()

    regix.first().match(httpReq, cs)

    val method = cs[0][0]
    val path = cs[1][0]
    val headers = cs[2].zip(cs[3])

    println("$method $path")
    for (f in headers) {
        println("${f.first}=${f.second}")
    }
```
result:
````properties
GET /api/doma.php?pepik=123
Host=127.0.0.1:8080
User-Agent=Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/110.0
Accept=text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8
Accept-Language=en-US,en;q=0.5
Accept-Encoding=gzip, deflate, br
DNT=1
Connection=keep-alive
Cookie=JSESSIONID=0DF49810BD55112FA90891FE5E687394
Upgrade-Insecure-Requests=1
Sec-Fetch-Dest=document
Sec-Fetch-Mode=navigate
Sec-Fetch-Site=none
Sec-Fetch-User=?1
Sec-GPC=1
Pragma=no-cache
Cache-Control=no-cache
````