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

import static org.junit.jupiter.api.Assertions.expectThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

public class FieldNameServiceTest {
 
    @Test
    public void test_createFieldName_with_null() {
        final Throwable exception = expectThrows(NullPointerException.class, () -> {
            new FieldNameService().createFieldName(null);
        });
        assertEquals("fieldname is null", exception.getMessage());
    }

    @Test
    public void test_createFieldName() {
        assertEquals("simple field name", new FieldNameService().createFieldName("SIMPLE FIELD NAME").asString());
    }

    @Test
    public void test_createFieldName_twice_for_cache() {
        final String name = "once";
        final FieldName fieldName = new FieldNameService().createFieldName(name);
        assertSame(fieldName, new FieldNameService().createFieldName("once"));
    }

    @Test
    public void test_createFieldNamePath_twice_for_cache() {
        final FieldNameService service = new FieldNameService();
        final FieldName name = service.createFieldName("once");
        final FieldNamePath fieldNamePath = new FieldNameService().createFieldNamePath(name);
        assertSame(fieldNamePath, service.createFieldNamePath(service.createFieldName("once")));
    }
}
