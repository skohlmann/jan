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
import org.joda.time.DateTime;

public final class TimeConverterService {
    
    static final long MILLIS = 1_000;
    static final long NANOS = MILLIS * MILLIS;

    public LocalDateTime jodaDateTimeToJava8LocalDateTime(final DateTime dt) {
        assert dt != null;
        final long millisEpoche = dt.getMillis();
        final long millisOffset = dt.getZone().getOffset(dt);

        final long secondsEpoche = millisEpoche / MILLIS;
        final int nanosEpocheAdd = (int) ((millisEpoche % MILLIS) * NANOS);
        final ZoneOffset offset = ZoneOffset.ofTotalSeconds((int) (millisOffset / MILLIS));
        return LocalDateTime.ofEpochSecond(secondsEpoche, nanosEpocheAdd, offset);
    }
}
