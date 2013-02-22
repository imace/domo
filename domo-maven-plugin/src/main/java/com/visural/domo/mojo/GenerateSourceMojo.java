/**
 * Copyright (C) 2012 Richard Nichols <rn@visural.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.visural.domo.mojo;

import com.visural.domo.codegen.introspect.StandardTasks;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import java.io.File;
import org.apache.maven.project.MavenProject;

/**
 * @goal refresh
 * @phase generate-sources
 */
public class GenerateSourceMojo extends AbstractMojo {

    /**
     * The JSON config file containing database information to use for domo introspection
     * @parameter expression="${domo.db}" default-value="domo-db.json"
     */
    private String domoDatabase;
    /**
     * The target dir for source generation. 
     * @parameter expression="${domo.dest}" default-value="target/generated-sources/domo"
     */
    private String domoDest;
    /**
     * The configuration file with code gen settings. Optional, if missing 
     * all tables will be generated with defaults.
     * @parameter expression="${domo.config}" default-value="domo-config.json"
     */
    private String configFile;
    /**
     * @parameter expression="${project}"
     * @required
     */
    protected MavenProject project;

    public void execute() throws MojoExecutionException {
        try {
            File dest = new File(domoDest);
            StandardTasks.generateSource(domoDatabase, configFile, domoDest);
            this.project.addCompileSourceRoot(dest.getCanonicalPath());
        } catch (Throwable t) {
            throw new MojoExecutionException(t.getMessage(), t);
        }
    }
}
