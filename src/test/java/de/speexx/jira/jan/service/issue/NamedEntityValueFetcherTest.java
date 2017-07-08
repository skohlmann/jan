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


import com.atlassian.jira.rest.client.api.NamedEntity;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

public class NamedEntityValueFetcherTest {
    
    @Test
    public void simpleGet() {
        assertEquals("name", new NamedEntityValueFetcher().getValue((NamedEntity) () -> "name").get());
    }
    
    @Test
    public void simpleGetNull() {
        assertFalse(new NamedEntityValueFetcher().getValue((NamedEntity) () -> null).isPresent());
    }
    
    @Test
    public void wrongType() {
        final Throwable exception = assertThrows(UnsupportedSourceTypeException.class, () -> {
            new NamedEntityValueFetcher().getValue("No NamedEntity");
        });
        assertEquals("Value type class java.lang.String not supported by de.speexx.jira.jan.service.issue.NamedEntityValueFetcher", exception.getMessage());
    }
}
