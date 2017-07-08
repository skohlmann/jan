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


import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import static de.speexx.jira.jan.service.issue.IssueCoreFieldConfig.IssueCoreFieldDescriptionEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IssueCoreFieldDescriptionEntryTest {
    
    @Test
    public void createInstanceWithoutFieldName() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new IssueCoreFieldDescriptionEntry(null, this.aliases, simpleMethod(), false, Class.class);
        });
        assertEquals("Fieldname is null", exception.getMessage());
    }

    @Test
    public void createInstanceWithoutIssueGetterMethod() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new IssueCoreFieldDescriptionEntry(new FieldName("key"), this.aliases, null, false, Class.class);
        });
        assertEquals("FieldNameMethod is null", exception.getMessage());
    }

    @Test
    public void createInstanceWithoutCast() {
        final Throwable exception = assertThrows(IllegalArgumentException.class, () -> {
            new IssueCoreFieldDescriptionEntry(new FieldName("key"), this.aliases, simpleMethod(), false, null);
        });
        assertEquals("Cast is null", exception.getMessage());
    }

    @Test
    public void createInstanceWithoutAliasNames() {
        final IssueCoreFieldDescriptionEntry entry 
                = new IssueCoreFieldDescriptionEntry(new FieldName("key"), null, simpleMethod(), false, DateTimeValueFetcher.class);
        assertTrue(entry.getFieldNameAliases().isEmpty());
    }

    final Set<FieldName> aliases = Collections.emptySet();
    Method simpleMethod() {
        try {
            return Class.class.getMethod("toString");
        } catch (final NoSuchMethodException | SecurityException ex) {
            throw new AssertionError(ex.getMessage());
        }
    }
}
