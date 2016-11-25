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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class IssueCoreFieldConfigLoaderTest {
    
    @Test
    public void loadConfig() {
        final IssueCoreFieldConfig config = new IssueCoreFieldConfigLoader().loadConfig();
        
        final Iterable<FieldName> iterable = () -> config.fieldNames();
        final Set<FieldName> names = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toSet());
        
        assertTrue(names.contains(new FieldName("key")));
        assertTrue(names.contains(new FieldName("issuekey")));
        assertFalse(names.contains(new FieldName(" summary"))); // not trimmed

        assertEquals("getUpdateDate", config.getIssueGetter(new FieldName("updateDATE")).get().getName());
        assertEquals(DateTimeValueFetcher.class, config.getValueFetcher(new FieldName("createdDate")).get().getClass());

        assertTrue(config.isIgnorable(new FieldName("key")).get());
        assertFalse(config.isIgnorable(new FieldName("summary")).get());
    }

    @Test
    public void unknownFieldName() {
        final IssueCoreFieldConfig config = new IssueCoreFieldConfigLoader().loadConfig();
        assertFalse(config.isIgnorable(new FieldName("xyz")).isPresent());
    }
}
