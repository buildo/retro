[ ![Download](https://api.bintray.com/packages/buildo/sbt-plugins/sbt-buildo/images/download.svg) ](https://bintray.com/buildo/maven/sbt-buildo/_latestVersion)

# sbt-buildo
sbt plugin to share settings and dependencies across scala projects in buildo

## How to use
Add this to your `project/plugins.sbt`

```scala
addSbtPlugin("io.buildo" % "sbt-buildo" % "latest.integration")
```

Then in `build.sbt`

```scala
inThisBuild(Seq(
  name := "cool-project",
  libraryDependencies ++= defaultDependencies
))
```

This plugins makes some common dependencies available. If you just want the default dependencies, import them in bulk using `defaultDependencies`.

If you prefer to cherry-pick, refer to https://github.com/buildo/sbt-buildo/blob/master/src/main/scala/buildo/CommonDependenciesSettingPlugin.scala
