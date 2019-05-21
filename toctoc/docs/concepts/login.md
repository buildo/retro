---
id: login
title: Login
---

A common kind of credential is the couple

```plaintext
(username, password)
```

where `username` uniquely identifies a subject and `password` is a secret known
**only** to the subject.

The lifecycle of login credentials must be handled with care.

Passwords must:

- Not be persisted anywhere by any agent other than the subject itself.
- Be transmitted using **cryptographically secure** transports.
