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
package de.speexx.jira.jan.service.issue;

import de.speexx.jira.jan.service.time.TimeConverterService;
import java.util.Optional;
import org.joda.time.DateTime;


/**
 * The implementation fetch a Joda {@link DateTime} instance from a data source
 * which is itself must be a Joda {@code DateTime}. The {@code DateTime} is
 * converted into a Java 8 {@link LocalDateTime}.
 */
class DateTimeValueFetcher implements ValueFetcher {

    static final TimeConverterService TIME_CONVERTER = new TimeConverterService();

    /**
     * @param dataSource can be {@code null}.
     * @return never {@code null}.
     * @throws UnsupportedSourceTypeException if and only if <em>dataSource</em> is
     *                                        not of type Joda {@link DateTime}.
     */
    @Override
    public Optional<Object> getValue(final Object dataSource) {
        if (dataSource == null) {
            return Optional.empty();
        }
        if (dataSource instanceof DateTime) {
            return Optional.ofNullable(TIME_CONVERTER.jodaDateTimeToJava8LocalDateTime(((DateTime) dataSource)));
        }
        throw new UnsupportedSourceTypeException("Value type " + dataSource.getClass() + " not supported by " + this.getClass().getName());
    }
}
