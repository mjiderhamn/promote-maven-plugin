package se.jiderhamn.promote.maven.plugin.promote;

import java.io.File;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import static org.codehaus.plexus.util.StringUtils.isBlank;

/**
 * @author Mattias Jiderhamn
 */
class PromoteUtils {

  /** File in which artifact information is stored between the build and the promotion */
  static final String FILENAME = "promotable-artifacts.properties";

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
    if(artifact.getFile() != null) // TODO Return Empty map if null?
      output.put(prefix + "file", relativize(basePath, artifact.getFile()));
    
    // TODO ?
    output.put(prefix + "baseVersion", artifact.getBaseVersion());
    
    return output;
  }

  static Artifact fromProperties(Properties input, String prefix, URI basePath) {
    prefix = fixPrefix(prefix);

    if(input == null || ! input.containsKey(prefix + "id"))
      return null;
    
    // String id = input.getProperty(prefix + "id");
    String groupId = input.getProperty(prefix + "groupId");
    String artifactId = input.getProperty(prefix + "artifactId");
    String version = input.getProperty(prefix + "version"); // TODO Use version from release:prepare
    String type = input.getProperty(prefix + "type");
    String scope = input.getProperty(prefix + "scope");
    String classifier = input.getProperty(prefix + "classifier");
    String relativePath = input.getProperty(prefix + "file");
    
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
    final String extension = FileUtils.extension(path);
    if(isBlank(type)) {
      type = extension;
    }
    DefaultArtifactHandler handler = new DefaultArtifactHandler(type);
    handler.setExtension(extension);
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
}