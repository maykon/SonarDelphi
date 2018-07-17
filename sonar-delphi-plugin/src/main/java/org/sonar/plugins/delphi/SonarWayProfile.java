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

import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RuleFinder;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.delphi.checks.CheckList;

/**
 * Profile "Sonar way" is activated by default.
 * It defines a short list of rules which are supposed to detect bugs and pitfalls in Delphi code.
 */
public class SonarWayProfile extends ProfileDefinition {

  // RuleFinder is only deprecated on scanner side, we can continue to use it on server side.
  private final RuleFinder ruleFinder;
  public static final String PROFILE_NAME = "Sonar way";
  public static final String PATH_TO_JSON = "org/sonar/l10n/delphi/rules/delphi/Sonar_way_profile.json";

  public SonarWayProfile(RuleFinder ruleFinder) {
    this.ruleFinder = ruleFinder;
  }

  @Override
  public RulesProfile createProfile(ValidationMessages messages) {
    RulesProfile profile = RulesProfile.create(PROFILE_NAME, DelphiLanguage.KEY);

    loadActiveKeysFromJsonProfile(profile);

    return profile;
  }

  private void loadActiveKeysFromJsonProfile(RulesProfile rulesProfile) {
    for (String ruleKey : JsonProfileReader.ruleKeys(PATH_TO_JSON)) {
      Rule rule = ruleFinder.findByKey(CheckList.REPOSITORY_KEY, ruleKey);
      rulesProfile.activateRule(rule, null);
    }
  }

}