package se.jiderhamn.promote.maven.plugin.promote;

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
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.PropertyUtils;

/**
 * This Mojo is expected to be executed as the preparationGoals of release:prepare, so that the release version can be
 * written
 */
@Mojo( name = WriteVersionMojo.NAME, requiresProject = true)
public class WriteVersionMojo extends AbstractMojo {

  /** Goal name */
  public static final String NAME = "write-version";
  
  /**
   * The maven project.
   */
   @Parameter(property = "project")
   private MavenProject project;
  
  public void execute() throws MojoExecutionException {
    String releaseVersion = project.getVersion();
    File promotePropertiesFile = PromoteUtils.getPromotePropertiesFile(project);
    if(promotePropertiesFile.exists()) {
      Properties props = PropertyUtils.loadProperties(promotePropertiesFile);
      props.setProperty(PromoteUtils.RELEASE_VERSION, releaseVersion);
    }
    else {
      getLog().warn("Cannot find " + promotePropertiesFile + ". Remember to run the " + 
          MakePromotableMojo.NAME + " goal after building the artifacts.");
    }
  }
}
