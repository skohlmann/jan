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
package de.speexx.jira.jan.command.issuefieldanalyzer;

import org.codehaus.jettison.json.JSONException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class JsonRuntimeExceptionTest {
    
    @Test
    public void message_from_cause() {
        final JSONException je = new JSONException("test");
        final JsonRuntimeException jre = new JsonRuntimeException(je);
        assertEquals("test", jre.getMessage());
    }
    
    @Test
    public void message_from_cause_not_given() {
        final JsonRuntimeException jre = new JsonRuntimeException(null);
        assertNull(jre.getMessage());
    }
}
