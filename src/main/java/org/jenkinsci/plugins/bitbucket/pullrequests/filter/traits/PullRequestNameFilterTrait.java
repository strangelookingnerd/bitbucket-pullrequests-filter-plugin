/*
 * The MIT License
 *
 * Copyright (c) 2021, Krzysztof Kacprzak.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.bitbucket.pullrequests.filter.traits;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.scm.api.SCMHeadCategory;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;
import jenkins.scm.impl.trait.Discovery;
import org.jenkinsci.plugins.bitbucket.pullrequests.filter.filters.title.PullRequestTitlePhraseExistsFilter;
import org.jenkinsci.plugins.bitbucket.pullrequests.filter.filters.title.PullRequestTitlePhraseNotExistsFilter;
import org.jenkinsci.plugins.bitbucket.pullrequests.filter.utils.filters.StringFilter;
import org.jenkinsci.plugins.bitbucket.pullrequests.filter.utils.filters.TypeFilter;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

/**
 * A {@link Discovery} trait for Bitbucket source that will blacklist pull requests matching any specified filter.
 *
 * @since 0.1.0
 */
public class PullRequestNameFilterTrait extends SCMSourceTrait {

    private int strategyId;

    private String phrase;
    private boolean ignoreCase;
    private boolean regex;

    /**
     * Constructor.
     */
    @DataBoundConstructor
    public PullRequestNameFilterTrait(int strategyId, String phrase, boolean ignoreCase, boolean regex) {
        this.strategyId = strategyId;
        this.phrase = phrase;
        this.ignoreCase = ignoreCase;
        this.regex = regex;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public int getStrategyId() {
        return this.strategyId;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public String getPhrase() {
        return this.phrase;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    @SuppressWarnings("unused") // used by Jelly EL
    public boolean isRegex() {
        return regex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean includeCategory(@Nonnull SCMHeadCategory category) {
        return category.isUncategorized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        StringFilter filter = createFilter();
        if (strategyId == 1) {
            context.withFilter(new PullRequestTitlePhraseNotExistsFilter(filter));
        } else if (strategyId == 2) {
            context.withFilter(new PullRequestTitlePhraseExistsFilter(filter));
        }
    }

    /**
     * Create a filter to validate the data of pull request.
     *
     * @return A {@link TypeFilter} instance to validate the extracted data from pull request.
     */
    protected StringFilter createFilter() {
        try {
            if (regex) {
                int regexFlags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
                return new StringFilter(phrase != null ? Pattern.compile(phrase, regexFlags) : null);
            }
            return new StringFilter(phrase, ignoreCase);
        } catch (Throwable t) {
            return null;
        }
    }

    @Extension
    @Discovery
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Filter by pull requests title";
        }

        /**
         * Populates the strategy options.
         *
         * @return the strategy options.
         */
        @Nonnull
        @Restricted(NoExternalUse.class)
        @SuppressWarnings("unused") // stapler
        public ListBoxModel doFillStrategyIdItems() {
            ListBoxModel result = new ListBoxModel();
            result.add("Accept all pull requests", "0");
            result.add("Ignore pull request when found the phrase in the title", "1");
            result.add("Only when the pull request title contains the phrase", "2");
            return result;
        }


        /**
         * Validate the inputs
         *
         * @param phrase      The phrase or the regular expression as pattern to matching
         * @param ignoreCase  Ignore case sensitivity
         * @param regex       Treat the phrase as regular expression
         * @param testMatcher The subject to validate by the pattern or the phrase
         * @return validation status
         */
        @Nonnull
        @Restricted(NoExternalUse.class)
        public FormValidation doTestPhrase(@QueryParameter("phrase") final String phrase,
                                           @QueryParameter("ignoreCase") final boolean ignoreCase,
                                           @QueryParameter("regex") final boolean regex,
                                           @QueryParameter("testMatcher") final String testMatcher) {
            try {
                StringFilter filter;
                if (regex) {
                    filter = new StringFilter(Pattern.compile(phrase, ignoreCase ? Pattern.CASE_INSENSITIVE : 0));
                } else {
                    filter = new StringFilter(phrase, ignoreCase);
                }
                if (filter.accepted(testMatcher)) {
                    return FormValidation.ok("The phrase is valid and matches!");
                } else {
                    return FormValidation.warning("The phrase is valid but not matches!");
                }
            } catch (Throwable t) {
                return FormValidation.error("Invalid phrase: " + t.getMessage());
            }
        }
    }

}