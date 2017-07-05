package com.mymo.grunt;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class GruntTaskBuilder extends Builder implements SimpleBuildStep {
    private final String task;

    @DataBoundConstructor
    public GruntTaskBuilder(String task) {
        this.task = task;
    }

    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        try {
            launcher.launch()
                    .cmdAsSingleString(String.format("%s %s", getDescriptor().getGruntHome(), task))
                    .join();
        } catch (IOException | InterruptedException e) {
            listener.getLogger().append(e.getMessage());
        }
    }

    @Override
    public GruntTaskBuildStepDescriptor getDescriptor() {
        return (GruntTaskBuildStepDescriptor) super.getDescriptor();
    }

    public String getTask() {
        return task;
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

