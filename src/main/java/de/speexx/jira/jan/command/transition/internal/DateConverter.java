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
package de.speexx.jira.jan.command.transition.internal;

import com.beust.jcommander.IStringConverter;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Converts a date string in form of ISO 8601 or other date formats to a
 * {@code LocalDate}.
 * <p>The implementation supports a lazy written format in form of
 * YYYY-MM-DD, YYYY-M-D, DD.MM.YYYY, D.M.YYYY, MM/DD/YYYY or M/D/YYYY.
 * Also the format YYYY-MM, MM.YYYY and MM/YYYY (lazy included) are supported.
 * In case of no given date, da first day of the month is used implicit.</p>
 * <p>Also the year only is supported. In case of the year only, the implementation
 * return first of January of the given year.</p>
 */
public class DateConverter implements IStringConverter<LocalDate>{
    
    static final String YEAR_ONLY_REGEX = "[12]\\d{3}";
    static final String MONTH_REGEX = "[01]?\\d";
    static final String ISO_DELIMITER = "-";
    static final String GERMAN_DELIMITER = "\\.";
    static final String US_DELIMITER = "/";

    private static final Pattern YEAR_ONLY_PATTERN = Pattern.compile("^" + YEAR_ONLY_REGEX);
    private static final Pattern YEAR_MONTH_ISO_PATTERN =
            Pattern.compile("^" + YEAR_ONLY_REGEX + ISO_DELIMITER + MONTH_REGEX);
    private static final Pattern YEAR_MONTH_GERMAN_PATTERN =
            Pattern.compile("^" + MONTH_REGEX + GERMAN_DELIMITER + YEAR_ONLY_REGEX);
    private static final Pattern YEAR_MONTH_US_PATTERN =
            Pattern.compile("^" + MONTH_REGEX + US_DELIMITER + YEAR_ONLY_REGEX);
    
    static final DateTimeFormatter LAZY_ISO_LOCAL_DATE = new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                .toFormatter();

    static final DateTimeFormatter LAZY_GERMAN_LIKE_LOCAL_DATE = new DateTimeFormatterBuilder()
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                .appendLiteral('.')
                .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                .appendLiteral('.')
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .toFormatter();
    
    static final DateTimeFormatter LAZY_US_LIKE_LOCAL_DATE = new DateTimeFormatterBuilder()
                .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                .appendLiteral('/')
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                .appendLiteral('/')
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .toFormatter();

    private final Collection<DateTimeFormatter> formatter;
    
    public DateConverter() {
        this.formatter = Arrays.asList(LAZY_ISO_LOCAL_DATE, LAZY_GERMAN_LIKE_LOCAL_DATE, LAZY_US_LIKE_LOCAL_DATE);
    }
    
    @Override
    public LocalDate convert(final String parameter) {
        if (parameter == null) {
            return null;
        }

        if (YEAR_ONLY_PATTERN.matcher(parameter).matches()) {
            return LocalDate.of(Integer.parseInt(parameter), Month.JANUARY, 1);
        }
        if (YEAR_MONTH_ISO_PATTERN.matcher(parameter).matches()) {
            final String yearMonth[] = parameter.split(ISO_DELIMITER);
            return LocalDate.of(Integer.parseInt(yearMonth[0]), toMonthInt(yearMonth[1]), 1);
        }
        if (YEAR_MONTH_GERMAN_PATTERN.matcher(parameter).matches()) {
            final String yearMonth[] = parameter.split(GERMAN_DELIMITER);
            return LocalDate.of(Integer.parseInt(yearMonth[1]), toMonthInt(yearMonth[0]), 1);
        }
        if (YEAR_MONTH_US_PATTERN.matcher(parameter).matches()) {
            final String yearMonth[] = parameter.split(US_DELIMITER);
            return LocalDate.of(Integer.parseInt(yearMonth[1]), toMonthInt(yearMonth[0]), 1);
        }

        for (final DateTimeFormatter fmt : this.formatter) {
            try {
                return LocalDate.parse(parameter, fmt);
            } catch (final Exception e) {
                // can be ignored
            }
        }
        throw new IllegalArgumentException("Unable to get date for parameter value: " + parameter);
    }
    
    int toMonthInt(final String s) {
        if (s.length() != 1) {
            if (s.charAt(0) == '0') {
                switch (s.charAt(1)) {
                    case '1': return 1;
                    case '2': return 2;
                    case '3': return 3;
                    case '4': return 4;
                    case '5': return 5;
                    case '6': return 6;
                    case '7': return 7;
                    case '8': return 8;
                    case '9': return 9;
                    default: throw new IllegalArgumentException("Zero is illegal month value. Must be between 1 and 12.");
                }
            }
        }
        final int value = Integer.parseInt(s);
        if (value < 1 || value > 12) {
            throw new IllegalArgumentException(value + " is illegal month value. Must be between 1 and 12.");
        }
        return value;
    }
}
