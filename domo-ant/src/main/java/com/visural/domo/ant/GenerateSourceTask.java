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
package com.visural.domo.ant;

import com.visural.domo.codegen.introspect.StandardTasks;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class GenerateSourceTask extends Task {

    private String dbConnectFile;
    private String dbConfigFile;
    private String generatedSourceBase;

    public String getDbConfigFile() {
        return dbConfigFile;
    }

    public String getDbConnectFile() {
        return dbConnectFile;
    }

    public String getGeneratedSourceBase() {
        return generatedSourceBase;
    }

    public void setDbConfigFile(String dbConfigFile) {
        this.dbConfigFile = dbConfigFile;
    }

    public void setDbConnectFile(String dbConnectFile) {
        this.dbConnectFile = dbConnectFile;
    }

    public void setGeneratedSourceBase(String generatedSourceBase) {
        this.generatedSourceBase = generatedSourceBase;
    }

    @Override
    public void execute() throws BuildException {        
        try {
            StandardTasks.generateSource(dbConnectFile, dbConfigFile, generatedSourceBase);
        } catch (Throwable t) {
            throw new BuildException(t);
        }
    }
}
