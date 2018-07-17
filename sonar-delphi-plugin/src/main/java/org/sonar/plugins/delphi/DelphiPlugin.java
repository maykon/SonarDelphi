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

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarProduct;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.utils.Version;
import org.sonar.delphi.tree.symbols.GlobalVariableNames;
import org.sonar.delphi.tree.symbols.type.JQuery;
import org.sonar.plugins.delphi.external.EslintReportSensor;
import org.sonar.plugins.delphi.lcov.CoverageSensor;
import org.sonar.plugins.delphi.rules.EslintRulesDefinition;
import org.sonar.plugins.delphi.rules.DelphiRulesDefinition;

public class DelphiPlugin implements Plugin {

  // Subcategories

  private static final String GENERAL = "General";
  private static final String TEST_AND_COVERAGE = "Tests and Coverage";
  private static final String LIBRARIES = "Libraries";
  private static final String DELPHI_CATEGORY = "Delphi";

  // Global Delphi constants

  public static final String FILE_SUFFIXES_KEY = "sonar.delphi.file.suffixes";
  public static final String FILE_SUFFIXES_DEFVALUE = ".pas";

  public static final String PROPERTY_PREFIX = "sonar.delphi";

  public static final String LCOV_REPORT_PATHS = PROPERTY_PREFIX + ".lcov.reportPaths";
  public static final String LCOV_REPORT_PATHS_DEFAULT_VALUE = "";

  public static final String ENVIRONMENTS = GlobalVariableNames.ENVIRONMENTS_PROPERTY_KEY;
  public static final String ENVIRONMENTS_DEFAULT_VALUE = GlobalVariableNames.ENVIRONMENTS_DEFAULT_VALUE;

  public static final String GLOBALS = GlobalVariableNames.GLOBALS_PROPERTY_KEY;
  public static final String GLOBALS_DEFAULT_VALUE = GlobalVariableNames.GLOBALS_DEFAULT_VALUE;

  public static final String IGNORE_HEADER_COMMENTS = PROPERTY_PREFIX + ".ignoreHeaderComments";
  public static final Boolean IGNORE_HEADER_COMMENTS_DEFAULT_VALUE = true;

  public static final String DELPHI_EXCLUSIONS_KEY = PROPERTY_PREFIX + ".exclusions";
  public static final String DELPHI_EXCLUSIONS_DEFAULT_VALUE = "**/bin/**,**/*.~*,**/*.bak*";

  public static final String EXTERNAL_ANALYZERS_CATEGORY = "External Analyzers";
  public static final String EXTERNAL_ANALYZERS_SUB_CATEGORY = "Delphi";
  public static final String ESLINT_REPORT_PATHS = "sonar.eslint.reportPaths";

  @Override
  public void define(Context context) {
    boolean externalIssuesSupported = context.getSonarQubeVersion().isGreaterThanOrEqual(Version.create(7, 2));

    context.addExtensions(
      DelphiLanguage.class,
      DelphiSensor.class,
      DelphiExclusionsFileFilter.class,
      DelphiRulesDefinition.class,
      SonarWayRecommendedProfile.class,
      SonarWayProfile.class);

    context.addExtensions(
      PropertyDefinition.builder(LCOV_REPORT_PATHS)
        .defaultValue(LCOV_REPORT_PATHS_DEFAULT_VALUE)
        .name("LCOV Files")
        .description("Paths (absolute or relative) to the files with LCOV data.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .subCategory(TEST_AND_COVERAGE)
        .category(DELPHI_CATEGORY)
        .multiValues(true)
        .build(),

      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(FILE_SUFFIXES_DEFVALUE)
        .name("File Suffixes")
        .description("List of suffixes for files to analyze.")
        .subCategory(GENERAL)
        .category(DELPHI_CATEGORY)
        .multiValues(true)
        .onQualifiers(Qualifiers.PROJECT)
        .build(),

      PropertyDefinition.builder(DelphiPlugin.IGNORE_HEADER_COMMENTS)
        .defaultValue(DelphiPlugin.IGNORE_HEADER_COMMENTS_DEFAULT_VALUE.toString())
        .name("Ignore header comments")
        .description("True to not count file header comments in comment metrics.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .subCategory(GENERAL)
        .category(DELPHI_CATEGORY)
        .type(PropertyType.BOOLEAN)
        .build(),

      PropertyDefinition.builder(DelphiPlugin.ENVIRONMENTS)
        .defaultValue(DelphiPlugin.ENVIRONMENTS_DEFAULT_VALUE)
        .name("Delphi execution environments")
        .description("List of environments names. The analyzer automatically adds global variables based on that list. "
          + "Available environment names: " + DelphiPlugin.ENVIRONMENTS_DEFAULT_VALUE + ".")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .subCategory(GENERAL)
        .multiValues(true)
        .category(DELPHI_CATEGORY)
        .build(),

      PropertyDefinition.builder(DelphiPlugin.GLOBALS)
        .defaultValue(DelphiPlugin.GLOBALS_DEFAULT_VALUE)
        .name("Global variables")
        .description("List of global variables.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .subCategory(GENERAL)
        .multiValues(true)
        .category(DELPHI_CATEGORY)
        .build(),

      PropertyDefinition.builder(DelphiPlugin.JS_EXCLUSIONS_KEY)
        .defaultValue(DELPHI_EXCLUSIONS_DEFAULT_VALUE)
        .name("Delphi Exclusions")
        .description("List of file path patterns to be excluded from analysis of Delphi files.")
        .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
        .subCategory(GENERAL)
        .multiValues(true)
        .category(DELPHI_CATEGORY)
        .build()
    );

    if (!context.getRuntime().getProduct().equals(SonarProduct.SONARLINT)) {
      context.addExtension(CoverageSensor.class);
      context.addExtension(EslintReportSensor.class);

      if (externalIssuesSupported) {
        context.addExtension(EslintRulesDefinition.class);

        context.addExtension(
          PropertyDefinition.builder(ESLINT_REPORT_PATHS)
            .name("ESLint Report Files")
            .description("Paths (absolute or relative) to the JSON files with ESLint issues.")
            .onQualifiers(Qualifiers.MODULE, Qualifiers.PROJECT)
            .category(EXTERNAL_ANALYZERS_CATEGORY)
            .subCategory(EXTERNAL_ANALYZERS_SUB_CATEGORY)
            .multiValues(true)
            .build());
      }
    }

  }
}