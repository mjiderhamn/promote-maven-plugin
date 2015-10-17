# Promote Maven plugin

This is a Maven plugin that allows for promoting the artifacts of the previous snapshot build to a release.
 
## Goals

TODO `promote` = `se.jiderhamn:promote`
 
* `promote:make-promotable` Identifies the artifacts of the build, and writes them to `promotable-artifacts.properties` in the project directory.
 This should be invoked at the end of any snapshot build that are candidates for promotion.
* `promote:prepare` Configure the [preparationGoals](http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#preparationGoals)
 of `release:prepare` to be `promote:write-version` so that we can capture the version number of the release instead of
 doing a new build. 
* `promote:artifacts` Read the artifact list written by `promote:make-promotable` during the snapshot build and register 
 them as being the artifacts produced by the current build.
 
## Usage
Add this to your `pom.xml`:
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>se.jiderhamn</groupId>
        <artifactId>promote-maven-plugin</artifactId>
        <version>0.1.5</version>
        <!-- Automatically execute promote:make-promotable after each snapshot build -->
        <executions>
          <execution>
            <goals>
              <goal>make-promotable</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <artifactId>maven-release-plugin</artifactId>
        <configuration>
          <preparationGoals>promote:artifacts deploy:deploy</preparationGoals>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

Build your snapshot at usual, say
```
mvn clean package
```

When you release, do just `release:prepare` and not `release:perform`.