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
package de.speexx.jira.jan.service.time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeConverterServiceTest {

    @Test
    public void covertJodaToLocal() {
        final DateTime jdt = new DateTime(2016, 11, 25, 13, 23, 11, 451, DateTimeZone.forOffsetMillis(1_800_000));
        final LocalDateTime zdt = ZonedDateTime.of(2016, 11, 25, 13, 23, 11, 451 * 1_000_000, ZoneOffset.ofTotalSeconds(1_800)).toLocalDateTime();

        final TimeConverterService converter = new TimeConverterService();
        assertEquals(zdt, converter.jodaDateTimeToJava8LocalDateTime(jdt));
    }
}
