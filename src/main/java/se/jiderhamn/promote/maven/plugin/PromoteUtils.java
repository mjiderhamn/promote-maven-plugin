package se.jiderhamn.promote.maven.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import static org.codehaus.plexus.util.StringUtils.isBlank;

/**
 * @author Mattias Jiderhamn
 */
class PromoteUtils {

  /** Prefix to use when configuring goals programatically */
  public static final String GOAL_PREFIX = "promote:"; // "se.jiderhamn:promote:";

  /** File in which artifact information is stored between the build and the promotion */
  static final String FILENAME = "promotable-artifacts.properties";

  /** Name of property containing the released version */
  public static final String RELEASE_VERSION = "releaseVersion";

  private PromoteUtils() {}

  static Map<String, String> toMap(Artifact artifact, String prefix, URI basePath) {
    prefix = fixPrefix(prefix);

    Map<String, String> output = new LinkedHashMap<String, String>();
    output.put(prefix + "id", artifact.getId());
    output.put(prefix + "groupId", artifact.getGroupId());
    output.put(prefix + "artifactId", artifact.getArtifactId());
    output.put(prefix + "version", artifact.getVersion());
    if(artifact.getScope() != null)
      output.put(prefix + "scope", artifact.getScope()); // Irrelevant
    // scope is not relevant
    output.put(prefix + "type", artifact.getType());
    if(artifact.getClassifier() != null)
      output.put(prefix + "classifier", artifact.getClassifier());
    if(artifact.getFile() != null) // Will be null for POM artifacts
      output.put(prefix + "file", relativize(basePath, artifact.getFile()));

    // TODO ?
    output.put(prefix + "baseVersion", artifact.getBaseVersion());

    return output;
  }
  
  /** Parse list of attached {@link Artifact}s from {@link Properties} */
  static List<Artifact> attachedArtifactsFromProperties(Properties props, URI basePath) {
    List<Artifact> output = new ArrayList<Artifact>();
    
    for(int i = 0; ; i++) {
      Artifact attachedArtifact = PromoteUtils.fromProperties(props, "attached." + i, basePath);
      if(attachedArtifact != null) {
        output.add(attachedArtifact);
      }
      else 
        break; // No more attached artifacts
    }
    
    return output;
  }

  static Artifact fromProperties(Properties input, String prefix, URI basePath) {
    prefix = fixPrefix(prefix);

    if(input == null || ! input.containsKey(prefix + "id"))
      return null;

    // String id = input.getProperty(prefix + "id");
    String groupId = input.getProperty(prefix + "groupId");
    String artifactId = input.getProperty(prefix + "artifactId");
    String version = input.getProperty(prefix + "version");
    String type = input.getProperty(prefix + "type");
    String scope = input.getProperty(prefix + "scope");
    String classifier = input.getProperty(prefix + "classifier");
    String relativePath = input.getProperty(prefix + "file");

    // Compare to org.apache.maven.bridge.MavenRepositorySystem.createArtifact()

    Artifact output = new DefaultArtifact(groupId, artifactId, version, scope, type, classifier,
        newHandler(type, relativePath));

    if(relativePath != null) {
      File path = new File((basePath != null) ? basePath.resolve(relativePath).getPath() : relativePath);
      output.setFile(path);
    }

    return output;
  }

  private static String fixPrefix(String prefix) {
    if(prefix == null)
      prefix = "";
    else if(prefix.length() > 0 && ! prefix.endsWith("."))
      prefix += ".";
    return prefix;
  }

  private static ArtifactHandler newHandler(String type, String path) {
    final String extension = (path == null) ? "" : FileUtils.extension(path);
    if(isBlank(type)) {
      type = extension;
    }
    DefaultArtifactHandler handler = new DefaultArtifactHandler(type);
    if(! isBlank(extension)) {
      handler.setExtension(extension);
    }
    return handler;
  }


  /** Create path relative to base path */
  private static String relativize(URI basePath, File path) {
    return (basePath != null) ?
        basePath.relativize(path.toURI()).getPath() :
        path.getPath();
  }

  /** Get path to this utils property file for the supplied project */
  static File getPromotePropertiesFile(MavenProject project) {
    return new File(project.getBuild().getDirectory(), FILENAME);
  }

  /** Get {@link java.net.URI} of project target directory */
  static URI getTargetURI(MavenProject project) {
    final String target = project.getBuild().getDirectory();
    return new File(target).toURI(); // Path to which we want to create relative paths
  }

  /** Write properties to file named {@link #FILENAME} in target directory */
  static void writeProperties(Log log, MavenProject project, Properties properties) throws MojoExecutionException {
    try {
      File file = getPromotePropertiesFile(project);
      if (file.getParentFile().mkdirs()) {
        log.debug("Created missing target directory.");
      }
      log.info("Writing artifact information to " + file);
      properties.store(new FileOutputStream(file), "Generated by promote-maven-plugin");
      log.debug("Written artifact properties: " + properties);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Error writing artifacts to file", e);
    }
  }
}
