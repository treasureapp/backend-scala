# akka based http client

## initial learnings

## Request-Level Client Side API
According to [akka docs](http://doc.akka.io/docs/akka-http/current/scala/http/client-side/index.html) the [Request-Level Client Side API](http://doc.akka.io/docs/akka-http/current/scala/http/client-side/request-level.html#request-level-api) is ideal for most use cases.

### Future-Based Variant

Within the Request-Level Client Side API there are two options:

1. Future-Based Variant (simple)
1. Flow-Based Variant (full-blown stream infrastructure)

