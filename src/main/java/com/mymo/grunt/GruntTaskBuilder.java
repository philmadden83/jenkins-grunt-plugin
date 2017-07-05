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
    private static final int SUCCESS = 0;

    private final String task;
    private final boolean sudo;

    @DataBoundConstructor
    public GruntTaskBuilder(String task, boolean sudo) {
        this.task       = task;
        this.sudo       = sudo;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {

            int result = launcher.launch()
                    .cmdAsSingleString(buildCommand())
                    .stderr(listener.getLogger())
                    .stdout(listener.getLogger())
                    .pwd(build.getWorkspace())
                    .join();

            return result == SUCCESS;

        } catch (IOException | InterruptedException e) {
            listener.fatalError(e.getMessage());
        }

        return false;
    }

    private String buildCommand() {
        StringBuilder stringBuilder;

        if (isSudo()) {
            stringBuilder = new StringBuilder(String.format("sudo %s %s", getDescriptor().getGruntHome(), getTask()));
        } else {
            stringBuilder = new StringBuilder(String.format("%s %s", getDescriptor().getGruntHome(), getTask()));
        }

        return stringBuilder.toString();
    }

    public String getTask() {
        return task;
    }

    public boolean isSudo() {
        return sudo;
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

