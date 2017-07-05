package com.mymo.grunt;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class GruntTaskBuilder extends Builder {
    private static final String DEBUG_FLAG      = "debug";
    private static final String FORCE_FLAG      = "force";
    private static final String BASE_FLAG       = "flag";
    private static final String VERBOSE_FLAG    = "verbose";
    private static final String GRUNTFILE_FLAG  = "gruntfile";

    private final String task;
    private final String base;
    private final String gruntFile;
    private final String properties;

    private final boolean debug;
    private final boolean force;
    private final boolean verbose;

    @DataBoundConstructor
    public GruntTaskBuilder(String task, String base, String gruntFile, String properties,
                            boolean debug, boolean force, boolean verbose) {
        this.task       = task;
        this.base       = base;
        this.gruntFile  = gruntFile;
        this.properties = properties;
        this.debug      = debug;
        this.force      = force;
        this.verbose    = verbose;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {

            int result = launcher.launch()
                    .cmdAsSingleString(getCommand())
                    .join();

            return result == 0;

        } catch (IOException | InterruptedException e) {
            listener.fatalError(e.getMessage());
        }

        return false;
    }

    private String getCommand() {
        StringBuilder stringBuilder = new StringBuilder(String.format("%s %s", getDescriptor().getGruntHome(), task));

        if (base != null && !base.isEmpty()) {
            appendFlag(stringBuilder, BASE_FLAG, base);
        }

        if (gruntFile != null && !gruntFile.isEmpty()) {
            appendFlag(stringBuilder, GRUNTFILE_FLAG, gruntFile);
        }

        if (properties != null && !properties.isEmpty()) {
            for (String propArg : properties.split("\n")) {
                appendFlag(stringBuilder, propArg);
            }
        }

        if (debug) {
            appendFlag(stringBuilder, DEBUG_FLAG);
        }

        if (force) {
            appendFlag(stringBuilder, FORCE_FLAG);
        }

        if (verbose) {
            appendFlag(stringBuilder, VERBOSE_FLAG);
        }

        return stringBuilder.toString();
    }

    private static void appendFlag(final StringBuilder builder, final String flag, final String... opts) {
        builder.append(String.format(" --%s", flag));
        if (opts != null && opts.length > 0) {
            builder.append(String.format(" %s", opts));
        }
    }

    public String getTask() {
        return task;
    }

    public String getBase() {
        return base;
    }

    public String getGruntFile() {
        return gruntFile;
    }

    public String getProperties() {
        return properties;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public GruntTaskBuildStepDescriptor getDescriptor() {
        return (GruntTaskBuildStepDescriptor) super.getDescriptor();
    }

    @Extension
    public static final class GruntTaskBuildStepDescriptor extends BuildStepDescriptor<Builder> {
        private String gruntHome;

        public GruntTaskBuildStepDescriptor() {
            load();
        }

        public String getGruntHome() {
            return gruntHome;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Invoke top-level Grunt tasks";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            gruntHome = formData.getString("gruntHome");
            save();
            return super.configure(req,formData);
        }
    }
}

