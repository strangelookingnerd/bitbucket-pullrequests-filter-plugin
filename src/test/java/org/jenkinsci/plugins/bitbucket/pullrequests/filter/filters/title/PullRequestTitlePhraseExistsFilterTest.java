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
package org.jenkinsci.plugins.bitbucket.pullrequests.filter.filters.title;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequest;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketPullRequestSource;
import hudson.model.TaskListener;
import jenkins.scm.api.trait.SCMHeadFilter;
import org.jenkinsci.plugins.bitbucket.pullrequests.filter.utils.filters.StringFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestTitlePhraseExistsFilterTest {

    @Mock
    BitbucketSCMSourceRequest scmSourceRequest;

    @Mock
    BitbucketPullRequest pullRequest;

    @Mock
    BitbucketPullRequestSource pullRequestSource;

    @Mock
    BitbucketBranch branch;

    @Mock
    PullRequestSCMHead pullRequestSCMHead;

    @Mock
    TaskListener taskListener;

    @Mock
    PrintStream logger;

    @Before
    public void setUp() throws Throwable {
        when(taskListener.getLogger()).thenReturn(logger);
        when(pullRequest.getId()).thenReturn("1");
        when(pullRequest.getSource()).thenReturn(pullRequestSource);
        when(pullRequestSource.getBranch()).thenReturn(branch);
        when(branch.getName()).thenReturn("test-branch");
        when(pullRequestSCMHead.getBranchName()).thenReturn("test-branch");
        when(scmSourceRequest.listener()).thenReturn(taskListener);
        when(scmSourceRequest.getPullRequests()).thenReturn(Arrays.asList(pullRequest));
        when(scmSourceRequest.getPullRequestById(1)).thenReturn(pullRequest);
    }

    @Test
    public void testCaseSensitivePhrase() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("Test", false)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testNegativeCaseSensitivePhrase() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("test", false)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testCaseInsensitivePhrase() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("test", true)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testPhraseWithSpecialCharacters() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("$Test$ title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("$Test$")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testPhraseWithinSpecialCharacters() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("$Test$ title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("Test")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testPhraseAsPartOfWord() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("t")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testPhraseWithSpaces() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("Test title")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testNegativePhraseWithSpaces() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("Test title")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testNotExistsPhrase() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("Not exists")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testBlankPhrase() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("   ")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testEmptyPhrase() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter("")).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testNullPhrase() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter((String) null)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testExistsPattern() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter(Pattern.compile("^Te??.*"))).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    @Test
    public void testNotExistsPattern() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter(Pattern.compile("^te??$"))).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testEmptyPattern() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter(Pattern.compile(""))).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(true));
    }

    @Test
    public void testNullPattern() throws IOException, InterruptedException {
        // given
        when(pullRequest.getTitle()).thenReturn("Test title");

        // when
        boolean isExcluded = givenSCMHeadFilter(givenFilter((Pattern) null)).isExcluded(scmSourceRequest, pullRequestSCMHead);

        // then
        assertThat(isExcluded, is(false));
    }

    private SCMHeadFilter givenSCMHeadFilter(StringFilter filter) {
        return new PullRequestTitlePhraseExistsFilter(filter);
    }

    private StringFilter givenFilter(Pattern pattern) {
        return new StringFilter(pattern);
    }

    private StringFilter givenFilter(String phrase, boolean ignoreCase) {
        return new StringFilter(phrase, ignoreCase);
    }

    private StringFilter givenFilter(String phrase) {
        return givenFilter(phrase, true);
    }
}