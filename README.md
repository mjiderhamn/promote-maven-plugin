# Promote Maven plugin

This is a Maven plugin that allows for promoting the artifacts of the previous snapshot build to a release.
 
## Usage
Add this to your `pom.xml`:
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>se.jiderhamn</groupId>
        <artifactId>promote-maven-plugin</artifactId>
        <version>1.0.0</version>
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

Note that `release:perform` should *not* be invoked. For that reason, you may also want to add `release:clean`.

## Goals

* `promote:make-promotable` Identifies the artifacts of the build, and writes them to `promotable-artifacts.properties` in the project directory.
 This should be invoked at the end of any snapshot build that are candidates for promotion.
* `promote:prepare` Configures the [preparationGoals](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#preparationGoals)
 and [completionGoals](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#completionGoals)
 to perform the release by invoking `promote:artifacts` + `deploy:deploy` 
* `promote:artifacts` Read the artifact list written by `promote:make-promotable` during the snapshot build and register 
 them as being the artifacts produced by the current build.
* `promote:no-op` Does nothing (no operation); used as dummy goal to avoid default goals.
 
 
