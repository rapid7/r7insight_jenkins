package com.logentries.jenkins;

import com.google.common.base.Strings;
import com.logentries.jenkins.Messages;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.OutputStream;

public class LogentriesBuildWrapper extends hudson.tasks.BuildWrapper {
    private final String token;
    private final String region;
    private String endpoint;

    /**
     * Create a new {@link LogentriesBuildWrapper}.
     *
     * @param token The token for the Rapid7 InsightOps log
     */
    @DataBoundConstructor
    public LogentriesBuildWrapper(String token, String region, String endpoint) {
        this.token = token;
        this.region = region;
        this.endpoint = endpoint;
    }

    /**
     * Gets the Rapid7 InsightOps token
     *
     * @return The Rapid7 InsightOps token
     */
    public String getToken() {
        return token;
    }

    /**
     * Gets the region to transmit the log data
     *
     * @return custom data ingestion host
     */
    public String getRegion() {
        return region;
    }

    /**
     * Gets the data ingestion endpoint
     *
     * @return data ingestion endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream decorateLogger(AbstractBuild build, OutputStream logger) {
        OutputStream decoratedOs = logger;

        try {
            if (Strings.isNullOrEmpty(endpoint)) {
                endpoint = String.format(LogentriesTcpTokenWriter.DATA_ENDPOINT_TEMPLATE, region);
            }
            LogentriesWriter logentriesWriter
                    = new AsynchronousLogentriesWriter(new LogentriesTcpTokenWriter(token, endpoint));
            decoratedOs = new LogentriesLogDecorator(logger, logentriesWriter);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            // Programmer error.
            e.printStackTrace();
        }
        // Should be the wrapped output stream if everything goes ok
        return decoratedOs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher,
                             BuildListener listener) throws IOException, InterruptedException {
        return new Environment() {
        };
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Registers {@link AnsiColorBuildWrapper} as a {@link hudson.tasks.BuildWrapper}.
     */
    @Extension
    public static final class DescriptorImpl extends BuildWrapperDescriptor {

        public DescriptorImpl() {
            super(LogentriesBuildWrapper.class);
            load();
        }

        public ListBoxModel doFillRegionItems() {
            ListBoxModel m = new ListBoxModel();
            m.add("Europe","eu");
            m.add("United States","us");
            m.add("Canada","ca");
            m.add("Australia","au");
            return m;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.DisplayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }
    }
}
