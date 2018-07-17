/*
 * SonarQube Delphi Plugin
 * Copyright (C) 2018 Maykon Luís Capellari
 * mailto:maykonluiscapellari AT gmail DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.delphi;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class DelphiLanguage extends AbstractLanguage {

  public static final String KEY = "delphi";

  private Configuration configuration;

  public DelphiLanguage(Configuration configuration) {
    super(KEY, "Delphi");
    this.configuration = configuration;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = configuration.getStringArray(DelphiPlugin.FILE_SUFFIXES_KEY);
    if (suffixes == null || suffixes.length == 0) {
      suffixes = StringUtils.split(DelphiPlugin.FILE_SUFFIXES_DEFVALUE, ",");
    }
    return suffixes;
  }

}