/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.gradle.plugins.workspace;

import groovy.json.JsonOutput;
import it.acsoftware.hyperiot.gradle.plugins.workspace.util.HyperIoTGradleWorkspaceUtil;
import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @Author Aristide Cittadino
 * HyperIoT Gradle Workspace Plugin
 * This plugin allows to automatically discover hyperiot projects and configure them.
 */
public class HyperIoTWorkspaceGradlePlugin implements Plugin<Settings>, BuildListener {

    public static final String HYPERIOT_WS_EXTENSION = "HyperIoTWorkspace";
    public static String EXCLUDE_PROJECT_PATHS = null;
    public static final String BND_TOOL_DEP_NAME = "biz.aQute.bnd:biz.aQute.bnd.gradle";

    public static final String FEATURES_SRC_FILE_PATH = "src/main/resources/features-src.xml";
    public static final String FEATURES_FILE_PATH = "src/main/resources/features.xml";
    private static Properties versionsProperties;

    static {
        EXCLUDE_PROJECT_PATHS = ".*/exam/.*|.*/build/.*|.*/target/.*|.*/bin/.*|.*/src/.*";
        versionsProperties = new Properties();
        try {
            versionsProperties.load(HyperIoTWorkspaceGradlePlugin.class.getClassLoader().getResourceAsStream("versions.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HyperIoTWorskpaceExtension extension;
    //reduntant in some method but it is needed in others
    private Settings settings;
    private MavenArtifactRepository maven2;

    /**
     * Step 1.
     * Adding extension to workspace and setup build listener
     *
     * @param settings
     */
    @Override
    public void apply(Settings settings) {
        this.settings = settings;
        this.extension = addWorkspaceExtension(settings);
        settings.getGradle().addBuildListener(this);

    }

    /**
     * Step2 2.
     * Adding repositories to settings buildscript.
     * Adding Dependency for BND tools
     * Adding Dependency for Karaf feature plugin
     * Apply plugin BND TOOLS
     *
     * @param gradle
     */
    public void buildStarted(Gradle gradle) {
        System.out.println("Build Started, updating build scripts...");
        settings.getBuildscript().getRepositories().add(settings.getBuildscript().getRepositories().mavenCentral());
        //adding maven Locale
        settings.getBuildscript().getRepositories().add(settings.getBuildscript().getRepositories().mavenLocal());
        settings.getBuildscript().getRepositories().add(settings.getBuildscript().getRepositories().maven(mavenArtifactRepository -> {
            mavenArtifactRepository.setUrl("http://central.maven.org/maven2");
        }));
        System.out.println("Adding required dependencies..");
        this.addBndGradleDep(settings.getBuildscript().getDependencies());
        System.out.println("Applying BND TOOLS plugin..");
        settings.getPlugins().apply("biz.aQute.bnd.workspace");
    }

    /**
     * Step 3
     * Adding projects to current build base on system property.
     *
     * @param settings
     */
    @Override
    public void settingsEvaluated(Settings settings) {
        Gradle gradle = settings.getGradle();
        String[] projectsToBuild = null;
        String modulesPath = settings.getRootProject().getProjectDir().getPath() + File.separator + extension.getModulesFolder();
        String skipProjectsDetectionStr = System.getProperty("skipProjectsDetection");
        boolean skipProjectsDetection = (skipProjectsDetectionStr != null) ? Boolean.parseBoolean(skipProjectsDetectionStr) : false;
        if (System.getProperty("projectsToBuild") != null)
            projectsToBuild = System.getProperty("projectsToBuild").split(",");

        if (skipProjectsDetection) {
            System.out.println("-- SKIPPING PROJECTS DETECTION --");
            return;
        }

        if (projectsToBuild == null) {
            //adding all projects found in modules folder
            addProjectsToWorkspace(modulesPath);
        } else {
            //adding projects specified by system property
            for (String project : projectsToBuild) {
                String module = transformInGradlePath(":" + extension.getModulesFolder() + ":" + project);
                includeProjectIntoWorkspace(settings, module);
            }
        }
    }

    /**
     * Step 4
     * Adding repositories to ROOT project along with BND TOOLS DEP and Karaf Feature DEP
     *
     * @param gradle
     */
    @Override
    public void projectsLoaded(Gradle gradle) {
        Project project = gradle.getRootProject();
        defineDefaultProperties(project);
        project.getBuildscript().getRepositories().add(project.getBuildscript().getRepositories().mavenCentral());
        project.getBuildscript().getRepositories().add(project.getBuildscript().getRepositories().mavenLocal());
        project.getBuildscript().getRepositories().add(project.getBuildscript().getRepositories().maven(mavenArtifactRepository -> {
            mavenArtifactRepository.setUrl("https://plugins.gradle.org/m2/");
        }));
        this.addBndGradleDep(project.getBuildscript().getDependencies());
    }

    /**
     * Step 5
     * Adding default task to the root project:
     * - DepList task, used to identify cycles inside workspace dependencies amd to calculate modularity measures
     * - buildHIT base build task which includes tasks for all subprojects
     *
     * @param gradle
     */
    @Override
    public void projectsEvaluated(Gradle gradle) {
        Project project = gradle.getRootProject();
        addDepListTask(project);
        addKarafFeaturesTask(project, project);
    }

    @Override
    public void buildFinished(BuildResult buildResult) {
        return;
    }

    /**
     * @param dependecies
     */
    private void addBndGradleDep(DependencyHandler dependecies) {
        String bndToolsVersion = versionsProperties.getProperty("bndToolVersion");
        dependecies.add("classpath", BND_TOOL_DEP_NAME + ":" + bndToolsVersion);
    }

    /**
     * @param settings
     * @return
     */
    private HyperIoTWorskpaceExtension addWorkspaceExtension(Settings settings) {
        ExtensionAware extensionAware = (ExtensionAware) settings.getGradle();
        ExtensionContainer extensionContainer = extensionAware.getExtensions();
        return extensionContainer.create(
                HYPERIOT_WS_EXTENSION, HyperIoTWorskpaceExtension.class, settings);
    }

    /**
     * @param modulesPath
     */
    private void addProjectsToWorkspace(String modulesPath) {
        String modulesFolderName = this.extension.getModulesFolder();
        try {
            List<String> projectsFound = new ArrayList<>();
            Files.walkFileTree(Paths.get(modulesPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                        throws IOException {
                    File file = path.toFile();
                    if (path.toString().matches(EXCLUDE_PROJECT_PATHS)) {
                        return FileVisitResult.SKIP_SIBLINGS;
                    } else if (file.isFile() && file.getName().endsWith("build.gradle")) {
                        projectsFound.add(file.getPath());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            projectsFound.forEach(filePath -> {
                filePath = filePath.replace(File.separator + "build.gradle", "");
                String modulesRelativePath = transformInGradlePath(filePath.substring(filePath.indexOf(modulesPath) + modulesPath.length()));
                String module = modulesFolderName + modulesRelativePath;
                includeProjectIntoWorkspace(settings, module);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path
     * @return
     */
    private String transformInGradlePath(String path) {
        return path.replaceAll("\\\\", ":").replaceAll("\\/", ":");
    }

    /**
     * @param projectModulePath
     */
    private void includeProjectIntoWorkspace(Settings settings, String projectModulePath) {
        System.out.println("Including :" + projectModulePath);
        settings.include(projectModulePath);
    }

    /**
     * Adding task for checking dependencies list
     *
     * @param rootProject
     */
    private void addDepListTask(Project rootProject) {
        Map<String, HashMap<String, Object>> depJson = new HashMap<>();

        rootProject.task("depList", new Action<Task>() {
            @Override
            public void execute(Task task) {
                task.doFirst(new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        Set<Project> subProjects = rootProject.getSubprojects();
                        subProjects.stream().forEach(p -> {
                            String projectName = p.getGroup() + ":" + p.getName() + ":" + p.getVersion();
                            String parentProjectName = p.getParent().getGroup() + ":" + p.getParent().getName() + ":" + p.getParent().getVersion();
                            if (depJson.get(projectName) == null) {
                                depJson.put(projectName, new HashMap<String, Object>());
                                depJson.get(projectName).put("parent", parentProjectName);
                                depJson.get(projectName).put("dependencies", new ArrayList<String>());
                                depJson.get(projectName).put("path", p.getBuildFile().getPath().replace("/build.gradle", ""));
                            }
                            //avoiding cycles on test dependencies
                            p.getConfigurations().stream().filter(conf -> !conf.getName().startsWith("test")).forEach(conf -> {
                                conf.getDependencies().stream().forEach(it -> {
                                    List<String> depList = (List<String>) depJson.get(projectName).get("dependencies");
                                    depList.add(it.getGroup() + ":" + it.getName() + ":" + it.getVersion());
                                });
                            });
                        });
                        String jsonStr = groovy.json.JsonOutput.prettyPrint(JsonOutput.toJson(depJson));
                        // define limits for output in order to be parse correctly
                        System.out.println("-- DEP LIST OUTPUT --");
                        System.out.println(jsonStr);
                        System.out.println("-- END DEP LIST OUTPUT --");
                    }
                });
            }
        });
    }

    /**
     * Adding task for recursively build projects
     *
     * @param rootProject
     */
    public void addBuildHITTask(Project rootProject) {
        rootProject.task("buildHIT", new Action<Task>() {
            @Override
            public void execute(Task task) {
                rootProject.getGradle().getIncludedBuilds().forEach(build -> task.dependsOn(build.task(":buildHIT")));
            }
        });
    }

    /**
     * @param project
     */
    private void addKarafFeaturesTask(Project rootProject, Project project) {
        File featuresSrcFile = new File(project.getProjectDir() + File.separator + FEATURES_SRC_FILE_PATH);
        //adding task to current project
        if (featuresSrcFile.exists()) {
            if (!project.hasProperty("generateFeatures"))
                addTaskToFeaturesProject(rootProject, project);
        }
        //search for children projects
        if (project.getSubprojects() != null && project.getSubprojects().size() > 0) {
            project.getSubprojects().forEach(subproject -> {
                addKarafFeaturesTask(rootProject, subproject);
            });
        }
    }

    private void addTaskToFeaturesProject(Project rootProject, Project project) {
        project.task("generateFeatures", task -> {
            task.doLast(innerTask -> {
                try {
                    String featuresSrcPath = project.getProjectDir().getAbsolutePath() + File.separator + FEATURES_SRC_FILE_PATH;
                    String featuresOutputPath = project.getProjectDir().getAbsolutePath() + File.separator + FEATURES_FILE_PATH;
                    String inputFileContent = new String(Files.readAllBytes(Paths.get(featuresSrcPath)));
                    String outputFileContent = inputFileContent;
                    Iterator<String> it = project.getProperties().keySet().iterator();
                    while (it.hasNext()) {
                        String key = it.next();
                        Object value = project.getProperties().get(key);
                        String token = "\\$\\{project." + key + "\\}";
                        if (value != null) {
                            outputFileContent = outputFileContent.replaceAll(token, value.toString());
                        }
                    }
                    Files.write(Paths.get(featuresOutputPath), outputFileContent.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void defineDefaultProperties(Project project) {
        //adding gradle plugin defined version
        HyperIoTGradleWorkspaceUtil.getAllDefinedVersions().stream().forEach(propertyName -> {
            project.getLogger().info("Setting global Property : {} = {}", propertyName, HyperIoTGradleWorkspaceUtil.getProperty(propertyName));
            project.getExtensions().getExtraProperties().set(propertyName, HyperIoTGradleWorkspaceUtil.getProperty(propertyName));
        });

        //overriding props with those specified inside the workspace
        HyperIoTGradleWorkspaceUtil.getWorkspaceDefinedVersions(project).stream().forEach(propertyName -> {
            project.getLogger().info("Setting Custom Workspace Property : {} = {}", propertyName, HyperIoTGradleWorkspaceUtil.getWorkspaceDefinedProperty(project, propertyName));
            project.getExtensions().getExtraProperties().set(propertyName, HyperIoTGradleWorkspaceUtil.getWorkspaceDefinedProperty(project, propertyName));
        });
    }
}
