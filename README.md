# DOMO

DOMO is a light-weight framework for working with SQL databases in Java.

It inspects your database and uses static code generation to generate stubs you can use as parent classes for your domain objects. 

The generated code is simple, human readable and easily inspected.

## Philosophy

There are a boatload of Java ORMs, frameworks and standards for working with databases already, so why build another one?

Domo has a different approach. It is an ORM from the "worse is better" school of thought.

Domo uses static code generation by inspecting your database tables and building stubs that you can use as parent classes. The code that is generated is very simple, human readable and easily inspected. 

You can pass classes based on a Domo stub to a Domo transaction, to query or update data over a standard JDBC connection.

Because there's no magic, you can create new objects with new Object(), serialize objects to disk using Java serialization, or to JSON, clone them and any other things you'd do with a Serializable POJO.

## Maturity

Domo is a young framework and as such has a few rough edges. The following describes roughly where it's at:

  * Most major features work correctly
  * It has been used in several real applications
  * Integration with major build environments is provided.
  * There are some features that have not been completely implemented, or have not stabilised as yet

It is still "alpha quality" in terms of stability and support. I haven't yet announced it officially and wouldn't recommend basing an important project on it.

## Compatibility

Domo works using standard JDBC meta-data API features, thus you may have success with any data source which implements a JDBC driver that supports meta-data retrieval.

It has been tested with the following databases using the standard JDBC drivers:

  * PostgreSQL
  * MySQL
  * Oracle
  * Microsoft SQL Server
  * H2 DB

Domo also has integration with the following third-party libraries:

  * Google Guice (@Transactional for AOP transactions)
  * Spring IOC (@Transactional for AOP transactions)

## Demo / Quick-Start

TODO

## Project Set-up

### Static Code Generation with DOMO

The first step in using DOMO is to set up the static code generation step in the build process. This is so stub classes get regenerated each time you build.

There are a number of ways to do this - 

  * via the commandline
  * via a Maven plugin
  * via an Ant task

#### Maven

Goals - 

  * domo:refresh
    * generates source code based on configuration and database tables
 
The following plugin config binds the refresh goal with your generate sources phase so that a standard compile will refresh the sources from the database structure. You may wish to do this manually for large projects to avoid the cost on every build.

The configuration options are shown with the default values. If you put `domo-config.json` and `domo-db.json` in the same folder as your `pom.xml` then you shouldn't need to specify any of these options.

    <build>
        <plugins>        
            <plugin>
                <groupId>com.visural</groupId>
                <artifactId>domo-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>refresh</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <databaseConfigPath>domo-db.json</databaseConfigPath>
                    <generateSourcesPath>target/generated-sources/domo</generateSourcesPath>
                    <configFile>domo-config.json</configFile>
                </configuration>                
                <dependencies>
                    <dependency>
                        <groupId>org.hsqldb</groupId>
                        <artifactId>hsqldb</artifactId>
                        <version>2.2.4</version>
                    </dependency>
                </dependencies>                
            </plugin>
        </plugins>
    </build>    

#### Ant Task

See the quick start project for example setup at (https://github.com/rjnichols/domo-ant-quickstart)[https://github.com/rjnichols/domo-ant-quickstart]

    <taskdef name="domo" classname="com.visural.domo.ant.GenerateSourceTask">
        <classpath>
            <filelist>
                <!-- include your jdbc driver here, e.g. postgres -->
                <file name="${basedir}/lib/PostgreSQLDriver/postgresql-9.1-901.jdbc4.jar"/>
                <file name="${basedir}/lib/domo-ant-1.0-SNAPSHOT-jar-with-dependencies.jar"/>
            </filelist>
        </classpath>
    </taskdef>
    
    <target name="-pre-compile">
        <domo dbConnectFile="${basedir}/domo-db.json" dbConfigFile="${basedir}/domo-config.json" generatedSourceBase="${basedir}/src/java"/>
    </target>

#### Command-line Generator

The command line generator is the "catch all" generator. If you can call a process from your build chain, the command line generator will work.

The *domo-core.jar* also includes the command line generator, so you would run:

TODO: The generator is pretty inflexible. There is also no default behavior, e.g. point at a database and generate all with defaults.

    java -jar domo-core.jar [database-config.json] [config-directory] [java-base-directory]

### Working With DOMO objects

To retrieve and update DOMO objects in a database, you need two things:

  * A *Db*
  * A *Transaction*

In DOMO *Db* is a service object which provide operations against a database. E.g. query, persist, delete. *Transaction* is an object that represents a transaction against a database on a given database connection, i.e. a number of operations followed by COMMIT or ROLLBACK. *Transaction* also supports the notion of savepoints similar to standard JDBC.

So to start working with DOMO you could write:

    
    // DOMO includes ways to manage connections, or 
    // you can provide your own JDBC connection.
    // We'll look at these features later.
    Connection con = ...; 

    Transaction tx = new JdbcTransaction(con);
    Db db = new JdbcDb(tx);
    List<MyTable> rows = db.query(MyTable.class, ....);
    for (MyTable row : rows) {
        // change data
        // ...
    }
    db.persist(rows);
    tx.commit();

## Querying

## Refreshing

## Persisting (insert or update)

### Primary Key Generation

## Trigger Interfaces

### AfterQueryHandler

### AfterRefreshHandler

### BeforePersistHandler / AfterPersistHandler

## Multi-row Update

## Deletes

## Lifecycle Considerations

## Integration

### Connection Pools

### Google Guice 

### Spring

## Multi-Schema/Catalog Support

## Multi-Database Support

DOMO allows you to set up multiple named ConnectionSources with different names. As such you can use a single DOMO environment to work with multiple databases.

>You can't execute a single coordinated transaction across multiple databases (JTA). See the section on "Multi-Database Coordinated Transactions".

TODO: more here

## Missing or Unsupported Features

### Multi-Database Coordinated Transactions (e.g. JTA)

Due to the complexity and extra dependencies, DOMO will not allow for transactions which span data sources, such as thoses supported in Java EE's JTA standard.

DOMO is designed to be light-weight and simple. Supporting JTA is complex.

DOMO does support multiple databases in a single application, so long as the transactions are coordinated separately (see "Multi-Database Support").

### Security Managers

DOMO is not compatibile with environments that use a security manager which limits or prevents the override of private/protected field access provisions by introspection.

DOMO peeks/pokes inside DOMO objects by overriding the normal accessor rules.

