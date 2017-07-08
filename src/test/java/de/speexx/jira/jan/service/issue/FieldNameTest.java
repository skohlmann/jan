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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class FieldNameTest {
 
    @Test
    public void test_simple_creation() {
        assertEquals("simple field name", new FieldName("SIMPLE FIELD NAME").asString());
    }
   
    @Test
    public void test_null_creation() {
        final Throwable exception = assertThrows(NullPointerException.class, () -> {
            new FieldName(null);
        });
        assertEquals("Name element is null", exception.getMessage());
    }
   
    @Test
    public void test_empty_name() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new FieldName(" ");
        });
        assertEquals("Name contains no visibile character", exception.getMessage());
    }
}
