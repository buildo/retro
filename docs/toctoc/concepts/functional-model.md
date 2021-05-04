---
id: functional-model
title: Functional Model
---

A **subject** is a human or machine user agent interacting with a secured
software application.

```plaintext
s ∈ S
```

A **credential** is a secret that uniquely identifies a subject.

```plaintext
c ∈ C
```

An **authentication domain** is a function `Fd` from credentials `C` subjects
`S`.

```plaintext
Fd: C ⟶ S
```

In particular an authentication domain can be represented by a set
`D := {(c, s) | Fd(c) = s}`, where `D ⊂ P(C x S)`.

Any fundamental authentication operation should be expressed in the context of
authentication domains, to be able to explicitly represent side effects.

## Operations

### Authenticate

The authenticate operation checks whether a given credential `c` identifies a
subject `s`. This operation could possibly modify a given authentication domain:
for example, in the OTP use case, a credential must be used only once.

```plaintext
Fa: D x C ⟶ D x S
```

### Register

The register operation adds a new association `(c, s)`. This means that the
subject `s` can be identified by the credential `c`.

```plaintext
Fr: D x C x S ⟶ D
```

### Unregister

The unregister operation removes any associations `(c, s)` for any given subject
`s`. This means that `s` will not be identifiable in the authentication domain.

```plaintext
Fu: D x S ⟶ D
```

For greater flexibility we can also define a companion operation that allows to
remove a single association `(c, s)`. This means that `s` will not be
identifiable by `c` in the authentication domain.

```plaintext
Fu': D x C ⟶ D
```

### Exchange

The exchange operations allows to use multiple authentication domains and
different credential types to implement complex authentication workflows. The
Token Based Authentication, for example, involves the use of login credentials
which can generate temporary access tokens.

```plaintext
Fx: Da x Db x Ca x Cb ⟶ Da x Db
```
