# Profiling Starter

`profiling-starter` is a Spring Boot starter that adds lightweight runtime method profiling through the `@Profiling` annotation.

## What it does

The starter logs execution metadata for annotated classes or methods:

- execution time,
- method parameters,
- return value,
- caller information,
- custom profiling message.

This helps inspect hot paths and diagnose behavior without modifying business code.

## How it works

### Default mode (recommended): Spring AOP infrastructure

By default, auto-configuration registers:

- `InfrastructureAdvisorAutoProxyCreator`
- a custom profiling advisor (`ProfilingPointcutAdvisor`)
- profiling advice adapter (`ProfilingAopMethodInterceptor`)

The advisor applies profiling only to beans/classes/methods matching the existing `@Profiling` semantics.

### Legacy mode (compatibility)

A legacy enhancer-based `BeanPostProcessor` path is still available for compatibility and can be enabled explicitly.

## Quick start

### Dependency

```xml
<dependency>
  <groupId>io.github.veaglev</groupId>
  <artifactId>profiling-spring-boot-starter</artifactId>
  <version>0.1.1</version>
</dependency>
```

### Minimal usage

```java
import org.profiling.Profiling;
import org.springframework.stereotype.Service;

@Service
@Profiling(message = "Processing user request")
public class UserService {
    public String loadUser(String id) {
        return "user-" + id;
    }
}
```

## Configuration reference

| Property | Default | Description |
|---|---|---|
| `profiling.enabled` | `true` | Enables/disables profiling auto-configuration globally. |
| `profiling.log-type` | `SIMPLE` | Output format (`SIMPLE`, `PRETTIER`). |
| `profiling.mode` | `AOP` | Wiring strategy (`AOP` default, `LEGACY` fallback). |

## Inclusion/exclusion and matching rules

Profiling advice is applied when:

- a bean class is annotated with `@Profiling`, or
- a concrete method on the class (or superclass) is annotated with `@Profiling`.

Infrastructure/support beans are excluded from profiling advisor application.

## Migration notes (manual enhancer -> infrastructure AOP)

- `AOP` is now the primary and default mode.
- `LEGACY` mode preserves historical enhancer behavior for compatibility-sensitive consumers.
- `@Profiling` annotation and logging semantics are preserved.

To force legacy mode:

```properties
profiling.mode=legacy
```

## Troubleshooting

- **Self-invocation**: internal method calls within the same bean bypass Spring proxies.
- **Final classes/methods**: class-based proxying cannot intercept final methods.
- **Proxy type expectations**: default mode uses class-based proxying (`proxyTargetClass=true`) for compatibility with previous behavior.
- **No logs visible**: verify logger configuration for `ProfilingLogger` level.

## Example output

`SIMPLE` example:

```text
+------------------+
| Profiling info:  |
+------------------+
| Processing user request
| Method: com.example.UserService.loadUser
| Params:
| [0] java.lang.String = 42
| Result: user-42
| Time: 1.20 ms
```

