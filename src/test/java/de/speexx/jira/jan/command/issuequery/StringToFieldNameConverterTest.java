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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class StringToFieldNameConverterTest {
    
    @Test
    public void test_simple_convert() {
        assertEquals("simple", new StringToFieldNameConverter().convert("Simple").asString());
    }

    @Test
    public void test_null_parameter() {
        final Throwable exception = assertThrows(NullPointerException.class, () -> {
            new StringToFieldNameConverter().convert(null);
        });
        assertEquals("Name is null", exception.getMessage());
    }
}
