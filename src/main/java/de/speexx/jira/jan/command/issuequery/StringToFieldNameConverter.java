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

import com.beust.jcommander.IStringConverter;
import de.speexx.jira.jan.service.issue.FieldName;
import de.speexx.jira.jan.service.issue.FieldNameService;
import java.util.Objects;

/**
 * Converts a command line parameter to a {@link FieldName} instance.
 */
public final class StringToFieldNameConverter implements IStringConverter<FieldName> {

    private static final FieldNameService FIELDNAME_SERVICE = new FieldNameService();

    /**    
     * @param name must not be {@code null}.
     * @return never {@code null}.
     * @throws NullPointerException if <em>name</em> is {@link null}.
     */
    @Override
    public FieldName convert(final String name) {
        if (Objects.isNull(name)) {
            throw new NullPointerException("Name is null");
        }
        return FIELDNAME_SERVICE.createFieldName(name);
    }    
}
