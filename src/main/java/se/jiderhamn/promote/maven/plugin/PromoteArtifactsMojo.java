package se.jiderhamn.promote.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;

/**
 * Mojo that sets the artifacts to be deployed
 */
@Mojo( name = PromoteArtifactsMojo.NAME, requiresProject = true)
public class PromoteArtifactsMojo extends AbstractMojo {

  public static final String NAME = "artifacts";

  /**
   * The maven project.
   */
   @Parameter(property = "project")
   private MavenProject project;
  
  public void execute() throws MojoExecutionException {
    File promotePropertiesFile = PromoteUtils.getPromotePropertiesFile(project);
    if(! promotePropertiesFile.exists()) {
      getLog().warn("Cannot find " + promotePropertiesFile + ". Remember to run the " + 
          MakePromotableMojo.NAME + " goal after building the artifacts.");
    }
    Properties props = PropertyUtils.loadProperties(promotePropertiesFile);
    getLog().info("Artifact information read from " + promotePropertiesFile);
    getLog().debug("Read properties: " + props);
    
    URI targetURI = PromoteUtils.getTargetURI(project);
    Artifact artifact = PromoteUtils.fromProperties(props, "artifact", targetURI);

    if(artifact != null) {
      validateArtifact(artifact);
      
      // Set artifact as being artifact of project
      getLog().info("Setting artifact: " + artifact);
      final String releasedVersion = PromoteUtils.getReleasedVersion(project.getBasedir(), artifact);
      artifact.setVersion(releasedVersion != null ? releasedVersion : project.getVersion());
      artifact.setRelease(true);
      project.setArtifact(artifact);
    }
    
    for(int i = 0; ; i++) {
      Artifact attachedArtifact = PromoteUtils.fromProperties(props, "attached." + i, targetURI);
      if(attachedArtifact == null)
        break; // No more attached artifacts
      
      validateArtifact(attachedArtifact);
      
      // Attach artifact to project
      getLog().info("Attaching artifact: " + attachedArtifact);
      final String releasedVersion = PromoteUtils.getReleasedVersion(project.getBasedir(), attachedArtifact);
      attachedArtifact.setVersion(releasedVersion != null ? releasedVersion : project.getVersion());
      attachedArtifact.setRelease(true);
      project.addAttachedArtifact(attachedArtifact);
    }
  }

  private void validateArtifact(Artifact artifact) {
    File file = artifact.getFile();
    if(file == null) {
      getLog().error("No file registered for artifact: " + artifact);
    }
    else if(! file.exists()) {
      getLog().error("File for artifact " + artifact + " does not exist: " + file);
    }
  }
}
