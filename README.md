# Promote Maven plugin

This is a Maven plugin that allows for promoting the artifacts of the previous snapshot build to a release. This allows you to speed
up the release process, by avoiding running the same compile-test-package phases again, when you already have a good build.

This plugin circumvents some of the precautions and conventions of Maven, and hopefully that is exactly what you aimed to when
you found this plugin.
 
## Usage
Add this to your `pom.xml`:
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>se.jiderhamn</groupId>
        <artifactId>promote-maven-plugin</artifactId>
        <version>1.0.2</version>
        <!-- Automatically execute promote:make-promotable after each snapshot build -->
        <executions>
          <execution>
            <goals>
              <goal>make-promotable</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
```

Build your snapshot as usual, say
```
mvn clean package
```

Now, in case you want to promote the snapshot into a release you would run `promote:prepare` + `release:prepare`. This
can be done either by running
```
mvn promote:prepare release:prepare
```
or by adding the following to the `<configuration>` of the `maven-release-plugin`
```xml
          <preparationGoals>promote:prepare</preparationGoals>
```
In the former case, the [preparationGoals](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#preparationGoals)
will be set to `promote:no-op` that does nothing. In both cases, the [completionGoals](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#completionGoals)
will be set to `promote:artifacts deploy:deploy`, that deploys the snapshot artifacts as release artifacts.

Note that `release:perform` should **not** be invoked. For that reason, you may also want to add `release:clean`.

## Jenkins
You will likely want to use this plugin together with [Continuous Integration](https://en.wikipedia.org/wiki/Continuous_integration),
so that the latest code is always built, and as soon as you are ready to release you'll just promote the last build.

For [Jenkins](https://jenkins.io/), it is recommended to combine this Maven plugin with the 
[Promoted Builds](https://wiki.jenkins-ci.org/display/JENKINS/Promoted+Builds+Plugin) Jenkins plugin.

Configure a normal Maven Jenkins project, and set _Build_ / _Goals and options_ as usual, say `clean install`. (This assumes
you've configured `promote:make-promotable` to be executed automatically as of above.)

[![Jenkins Build](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_build_65p.png)](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_build.png)

Create a manually triggered promotion (_Promote builds when..._ + _Only when manually approved_) and select a _Name_ and an _Icon_.
(Unfortunately, the _Name_ cannot currently contain any variables, which would have allowed hoovering the icon/badge to see the 
released version. Please vote for [JENKINS-31725](https://issues.jenkins-ci.org/browse/JENKINS-31725)!)

Use _Add Parameter_ to add 2 _String Parameter_ s, one named `releaseVersion` and another named `developmentVersion`
(as per the [`release:prepare` goal of the Maven Release Plugin](https://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html)).

[![Approval Parameters](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_promote_parameters_65p.png)](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_promote_parameters.png)

_Add action_: _Invoke top-level Maven targets_ and set _Goals_ to `release:clean promote:prepare release:prepare`.

[![Promote Action](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_promote_actions_65p.png)](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_promote_actions.png)

Now it will be possible to manually promote the latest `-SNAPSHOT` build, allowing you to set the version of the release and the
SNAPSHOT version of the next development iteration, just like you would with a normal Maven release. 

[![Promote Approve](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_promote_approve_65p.png)](https://cdn.rawgit.com/mjiderhamn/promote-maven-plugin/master/images/jenkins_promote_approve.png)

## Goals
These are the Maven goals defined by this plugin
* `promote:make-promotable` Identifies the artifacts of the build, and writes them to `promotable-artifacts.properties` in the project directory.
 This should be invoked at the end of any snapshot build that are candidates for promotion.
* `promote:prepare` Configures the [preparationGoals](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#preparationGoals)
 and [completionGoals](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#completionGoals)
 to perform the release by invoking `promote:artifacts` + `deploy:deploy` 
* `promote:artifacts` Read the artifact list written by `promote:make-promotable` during the snapshot build and register 
 them as being the artifacts produced by the current build.
* `promote:no-op` Does nothing (no operation); used as dummy goal to avoid default goals.

## Caveat
You should be aware that in case you have a multi module Maven project with "nested" packaging, i.e. one of your modules produces a 
WAR (`<packaging>war</packaging>`) that contains JARs produced by other modules in the same project, then the name of 
those `.jar`s inside the promoted `.war` will be the `-SNAPSHOT` that was promoted.

Example: You have a Maven multi module project that creates `foo-[VERSION].jar` which is included in `bar-[VERSION].war`.
When promote the SNAPSHOT build `1.2.3-SNAPSHOT` to a `1.2.3` release, then `bar-1.2.3.war` will 
contain `foo-1.2.3-SNAPSHOT.jar`.

If this is a problem, then this plugin is not for you.

Even though it would have been theoretically possible for the plugin to rename the JAR inside the WAR that would defeat the purpose
of the plugin. Unless we promote the exact same (binary) artifacts, it is per definition not a promotion but some kind of new 
build, and then you may as well use the ordinary [Maven Release Plugin](https://maven.apache.org/maven-release/maven-release-plugin/).