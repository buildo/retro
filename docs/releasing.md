---
id: releasing
title: Managing releases
---

We automatically publish all libraries in the monorepo to Sonatype on every
commit on `master`.

We use a modified version
[`sbt-ci-release`](https://github.com/olafurpg/sbt-ci-release) that handles
publishing multiple packages at once.

We publish both **stable** and **snapshot** releases, based on the strategy
detailed below.

## Snapshot releases

Snapshot releases are published for every library for every commit on master.
Their version is computed using a modified version of
[`sbt-dynver`](https://github.com/dwijnand/sbt-dynver) that handles tag prefixes
(see the next paragraph).

To use snapshot releases, projects must add Sonatype `snapshots` resolver to
their Sbt build:

```scala
resolvers += Resolver.sonatypeRepo("snapshots")
```

## Stable releases

Stable releases are published whenever there's a matching Git tag associated
with the current commit.

Each project in the monorepo can configure the `dynverTagPrefix` setting to
declare its matching tag.

> 👉 Example: `enumeroCore` and `enumeroCirce` use `enumero-` as their tag
> prefix, meaning that a stable release will be published whenever a Git tag
> starting with `enumero-` is found.

### Releasing a stable version

To release a new stable version of a library, push a Git tag with this pattern:
`<PREFIX>v<VERSION>`.

> 👉 Example: you can release the version 1.0.0 of `enumeroCore` and
> `enumeroCirce` by pushing the `enumero-v1.0.0` tag.

You can do this directly from the
[new release page on GitHub](https://github.com/buildo/retro/releases/new)

Stable releases are automatically synced with Maven Central, meaning you can
depend on them without adding any resolver to Sbt. The sync process usually
takes ~10 minutes.
