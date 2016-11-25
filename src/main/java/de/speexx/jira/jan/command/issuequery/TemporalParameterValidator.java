/* A tool to extract transition information from JIRA for analyzis (jan).
 *
 * Copyright (C) 2016 Sascha Kohlmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.speexx.jira.jan.command.issuequery;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_PARAMETER_NAME_SHORT;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_PARAMETER_NAME_LONG;


import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_NONE_LONG;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_NONE_SHORT;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_TIME_LONG;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_TIME_SHORT;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_DURATION_LONG;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_DURATION_SHORT;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_BOTH_LONG;
import static de.speexx.jira.jan.command.issuequery.ChangelogFieldFetcher.TEMPORAL_VALUE_BOTH_SHORT;
import java.util.Locale;

/**
 * Converter and validator for temporal parameters.
 */
public final class TemporalParameterValidator implements IParameterValidator, IStringConverter<TemporalChangeOutput> {

    /**
     * Check if the given parameterfor <tt>-t</tt> or <tt>--temporal</tt> follows
     * the required rule.
     * @param parameter must be <tt>-t</tt> or <tt>--temporal</tt>
     * @param value must be <tt>none</tt> or <tt>n</tt>, <tt>time</tt> or <tt>t</tt>,
     *              <tt>duration</tt> or <tt>d</tt> or <tt>both</tt> or <tt>b</tt>.
     * @throws ParameterException if <em>value</em> does not match the given rules.
     */
    @Override
    public void validate(final String parameter, final String value) throws ParameterException {
        if (TEMPORAL_PARAMETER_NAME_SHORT.equals(parameter)
                || TEMPORAL_PARAMETER_NAME_LONG.equals(parameter)) {
            if (!TEMPORAL_VALUE_NONE_LONG.equalsIgnoreCase(value)
                    && !TEMPORAL_VALUE_NONE_SHORT.equalsIgnoreCase(value)
                    && !TEMPORAL_VALUE_TIME_LONG.equalsIgnoreCase(value)
                    && !TEMPORAL_VALUE_TIME_SHORT.equalsIgnoreCase(value)
                    && !TEMPORAL_VALUE_DURATION_LONG.equalsIgnoreCase(value)
                    && !TEMPORAL_VALUE_DURATION_SHORT.equalsIgnoreCase(value)
                    && !TEMPORAL_VALUE_BOTH_LONG.equalsIgnoreCase(value)
                    && !TEMPORAL_VALUE_BOTH_SHORT.equalsIgnoreCase(value)) {
                throw new ParameterException("Illegal temporal parameter value: " + value);
            }
        }
    }

    /**
     * Transforms the temporal parameter value into a type safe <tt>enum</tt>.
     * If value doesn't match, the default value parameter
     * ({@link TemporalChangeOutput#DURATION}) will be returned.
     * @param value the value to convert.
     * @return a type safe <tt>enum</tt>.
     * @see #validate(java.lang.String, java.lang.String)  for supported values.
     */
    @Override
    public TemporalChangeOutput convert(final String value) {
        final String lower = value.toLowerCase(Locale.ENGLISH);
        if (lower.startsWith(TEMPORAL_VALUE_NONE_SHORT)) {
            return TemporalChangeOutput.NONE;
        } else if (lower.startsWith(TEMPORAL_VALUE_TIME_SHORT)) {
            return TemporalChangeOutput.TIME;
        } else if (lower.startsWith(TEMPORAL_VALUE_BOTH_SHORT)) {
            return TemporalChangeOutput.BOTH;
        }
        return TemporalChangeOutput.DURATION;
    }
}
