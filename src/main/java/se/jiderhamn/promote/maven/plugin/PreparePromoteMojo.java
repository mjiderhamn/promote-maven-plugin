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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo that configures the <a href="http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#completionGoals">completionGoals</a>
 * of the {@code release:prepare} goal to perform the actual promotion i.e. deployment of artifacts.
 * It also configures <a href="http://maven.apache.org/maven-release/maven-release-plugin/prepare-mojo.html#preparationGoals">preparationGoals</a>
 * to {@code promote:no-op}. This means this can either be provided on the command line, before <code>release:prepare</code>
 * or as the {@code preparationGoals} 
 */
@Mojo( name = "prepare", requiresProject = true /*, defaultPhase = LifecyclePhase.PROCESS_SOURCES */ )
public class PreparePromoteMojo extends AbstractMojo {

  /** The maven project */
  @Parameter(property = "project")
  private MavenProject project;

  public void execute() throws MojoExecutionException {
    project.getProperties().setProperty("preparationGoals", PromoteUtils.GOAL_PREFIX + NoOpMojo.NAME);
    project.getProperties().setProperty("completionGoals", 
        PromoteUtils.GOAL_PREFIX + PromoteArtifactsMojo.NAME + " deploy:deploy");

    // TODO Auto disable release:perform
  }
}
