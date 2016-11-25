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

import de.speexx.jira.jan.service.issue.FieldName;
import de.speexx.jira.jan.service.issue.FieldNamePath;
import de.speexx.jira.jan.service.issue.FieldNameService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.expectThrows;
import org.junit.jupiter.api.Test;


public class StringToFieldNamePathConverterTest {
    
    @Test
    public void test_split_string_to_FieldNamePath() {
        
        final FieldNameService service = new FieldNameService();
        
        final StringToFieldNamePathConverter converter = new StringToFieldNamePathConverter();
        final FieldNamePath path = converter.convert("simple::PATH:: element");
        final List<FieldName> elements = new ArrayList<>();
        path.forEach(element -> elements.add(element));
        assertTrue(Arrays.equals(new FieldName[] {service.createFieldName("simple"), 
                                                  service.createFieldName("path"),
                                                  service.createFieldName(" element")},
                                 elements.toArray(new FieldName[3])));
    }

    @Test
    public void test_null_parameter() {
        final Throwable exception = expectThrows(NullPointerException.class, () -> {
            new StringToFieldNamePathConverter().convert(null);
        });
        assertEquals("Path is null", exception.getMessage());
    }
}
