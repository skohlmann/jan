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

import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class FieldNamePathTest {
    
    @Test
    public void test_join_as_string() {
        final FieldNamePath path = 
                new FieldNamePath(new FieldName[] {new FieldName("simple "),
                                                   new FieldName("Path"),
                                                   new FieldName("ELEMENT")});
        assertEquals("simple ::path::element", path.asString());
        assertEquals(3, path.length());
    }

    @Test
    public void test_null_creation_with_FieldName_array() {
        final Throwable exception = assertThrows(NullPointerException.class, () -> {
            new FieldNamePath((FieldName[]) null);
        });
        assertEquals("pathElements is null", exception.getMessage());
    }

    @Test
    public void test_null_creation_with_FieldName_list() {
        final Throwable exception = assertThrows(NullPointerException.class, () -> {
            new FieldNamePath((List<FieldName>) null);
        });
        assertEquals("pathElements is null", exception.getMessage());
    }

    @Test
    public void test_creation_with_null_FieldName_entry_in_array() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new FieldNamePath(new FieldName[] {new FieldName("abc"), null});
        });
        assertTrue(exception.getMessage().startsWith("FieldName list contains null value: "));
    }

    @Test
    public void test_creation_with_null_FieldName_entry_in_list() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new FieldNamePath(Arrays.asList(new FieldName[] {new FieldName("abc"), null}));
        });
        assertTrue(exception.getMessage().startsWith("FieldName list contains null value: "));
    }

    @Test
    public void test_creation_with_no_root_in_array() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new FieldNamePath(new FieldName[] {});
        });
        assertTrue(exception.getMessage().startsWith("No root element given"));
    }

    @Test
    public void test_creation_with_no_root_in_list() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new FieldNamePath(Arrays.asList(new FieldName[] {}));
        });
        assertTrue(exception.getMessage().startsWith("No root element given"));
    }

    @Test
    public void test_get_path_without_root() {
        final FieldNamePath path = 
                new FieldNamePath(new FieldName[] {new FieldName("simple "),
                                                   new FieldName("ELEMENT")});
        assertEquals("element", path.getPathWithoutRoot().asString());
        assertEquals(1, path.getPathWithoutRoot().length());
    }

    @Test
    public void test_get_path_without_root_for_single_path() {
        final FieldNamePath path = 
                new FieldNamePath(new FieldName[] {new FieldName("single")});
        assertEquals("", path.getPathWithoutRoot().asString());
        assertEquals(0, path.getPathWithoutRoot().length());

        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            path.getPathWithoutRoot().getPathWithoutRoot();
        });
    }

    @Test
    public void test_getRootElement() {
        final FieldNamePath path = 
                new FieldNamePath(new FieldName[] {new FieldName("simple "),
                                                   new FieldName("Path"),
                                                   new FieldName("ELEMENT")});
        assertEquals("simple ", path.getRootElement().asString());
    }

}
