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
package org.jenkinsci.plugins.bitbucket.pullrequests.filter.utils.filters;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringFilter implements TypeFilter<String> {

    protected static final String PHRASES_SEPARATOR = ",;";

    private final Collection<Pattern> patterns;

    public StringFilter(String phrases) {
        this(phrases, true);
    }

    public StringFilter(String phrases, boolean ignoreCase) {
        int regexFlags = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;
        Collection<Pattern> patterns = transformPhrasesToPattern(transformPhraseToList(phrases), regexFlags);
        this.patterns = Collections.unmodifiableCollection(patterns);
    }

    public StringFilter(Pattern pattern) {
        this(pattern != null ? Collections.singletonList(pattern) : Collections.emptyList());
    }

    public StringFilter(Collection<Pattern> patterns) {
        this.patterns = Collections.unmodifiableCollection(patterns != null ? patterns : Collections.emptyList());
    }

    @Override
    public boolean canFilter() {
        return !getPatterns().isEmpty();
    }

    @Override
    public boolean accepted(String data) {
        if (!canFilter()) {
            return true;
        }

        if (data == null) {
            return false;
        }

        return getPatterns().stream().anyMatch(pattern -> pattern.matcher(data).matches());
    }

    public Collection<Pattern> getPatterns() {
        return patterns;
    }

    protected Collection<String> transformPhraseToList(String phrases) {
        String nonNullPhrases = StringUtils.trimToEmpty(phrases);
        String[] splittedPhrases = StringUtils.split(nonNullPhrases, PHRASES_SEPARATOR);
        return Arrays.stream(splittedPhrases)
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    protected Collection<Pattern> transformPhrasesToPattern(Collection<String> phrases, int flags) {
        if (phrases == null) {
            return Collections.emptyList();
        }

        return phrases.stream().map(phrase -> transformPhraseToPattern(phrase, flags)).collect(Collectors.toList());
    }

    protected Pattern transformPhraseToPattern(String phrase, int flags) {
        return Pattern.compile("(^|.*[^\\w])\\Q" + phrase + "\\E([^\\w].*|$)", flags);
    }

}