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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;

/**
 * TODO Document
 * TODO Always release:clean also
 */
@Mojo(name = MakePromotableMojo.NAME, requiresProject = true, defaultPhase = LifecyclePhase.PACKAGE)
public class MakePromotableMojo extends AbstractMojo {

  public static final String NAME = "make-promotable";

  /** The maven project */
  @Parameter(property = "project")
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    final URI targetURI = PromoteUtils.getTargetURI(project);

    List<Artifact> attachedArtifacts = project.getAttachedArtifacts();

    Properties artifactInfo = new Properties();
    int firstAttachedArtifactIndex = 0; // Assume no existing attached artifacts
    final File promotePropertiesFile = PromoteUtils.getPromotePropertiesFile(project);
    if(promotePropertiesFile.exists()) { // Existing promote properties exist, let's add to those
      artifactInfo = PropertyUtils.loadProperties(promotePropertiesFile);
      getLog().info("Existing artifact information read from " + promotePropertiesFile);
      getLog().debug("Read properties: " + artifactInfo);
      
      final List<Artifact> previouslyAttachedArtifacts = PromoteUtils.attachedArtifactsFromProperties(artifactInfo, targetURI);
      firstAttachedArtifactIndex = previouslyAttachedArtifacts.size(); // Continue index after existing
      // Avoid duplicates
      attachedArtifacts = new ArrayList<Artifact>(attachedArtifacts);
      attachedArtifacts.removeAll(previouslyAttachedArtifacts);
    }

    Artifact artifact = project.getArtifact();
    if(artifact != null) {
      getLog().info("Artifact: " + artifact.getId());
      Map<String, String> artifactProperties = PromoteUtils.toMap(artifact, "artifact", targetURI);
      getLog().debug("Artifact properties: " + artifactProperties);
      artifactInfo.putAll(artifactProperties);
    }
    else
      getLog().debug("No main artifact found");

    if(! attachedArtifacts.isEmpty()) {
      for(int i = 0; i < attachedArtifacts.size(); i++) {
        Artifact attachedArtifact = attachedArtifacts.get(i);
        getLog().info("Attached artifact: " + attachedArtifact.getId());
        Map<String, String> artifactProperties = PromoteUtils.toMap(attachedArtifact, "attached." + (i + firstAttachedArtifactIndex), targetURI);
        getLog().debug("Attached artifact properties: " + artifactProperties);
        artifactInfo.putAll(artifactProperties);
      }
    }

    PromoteUtils.writeProperties(getLog(), project, artifactInfo);

  }

}
