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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.delphi.se.SeCheck;
import org.sonar.plugins.delphi.api.CustomDelphiRulesDefinition;
import org.sonar.plugins.delphi.api.CustomRuleRepository;
import org.sonar.plugins.delphi.api.DelphiCheck;
import org.sonar.plugins.delphi.api.visitors.TreeVisitor;

/**
 * Wrapper around Checks Object to ease the manipulation of the different Delphi rule repositories.
 */
public class DelphiChecks {

  private static final Logger LOG = Loggers.get(DelphiSensor.class);

  private final CheckFactory checkFactory;
  private Set<Checks<DelphiCheck>> checksByRepository = Sets.newHashSet();

  private DelphiChecks(CheckFactory checkFactory) {
    this.checkFactory = checkFactory;
  }

  public static DelphiChecks createDelphiCheck(CheckFactory checkFactory) {
    return new DelphiChecks(checkFactory);
  }

  public DelphiChecks addChecks(String repositoryKey, Iterable<Class> checkClass) {
    checksByRepository.add(checkFactory
      .<DelphiChecks>create(repositoryKey)
      .addAnnotatedChecks(checkClass));

    return this;
  }

  public DelphiChecks addCustomChecks(@Nullable CustomDelphiRulesDefinition[] customRulesDefinitions,
                                          @Nullable CustomRuleRepository[] customRuleRepositories) {
    if (customRulesDefinitions != null) {
      LOG.warn("CustomDelphiRulesDefinition usage is deprecated. Use CustomRuleRepository API to define custom rules");
      for (CustomDelphiRulesDefinition rulesDefinition : customRulesDefinitions) {
        addChecks(rulesDefinition.repositoryKey(), ImmutableList.copyOf(rulesDefinition.checkClasses()));
      }
    }

    if (customRuleRepositories != null) {
      for (CustomRuleRepository repo : customRuleRepositories) {
        addChecks(repo.repositoryKey(), repo.checkClasses());
      }
    }

    return this;
  }

  private List<DelphiChecks> all() {
    List<DelphiChecks> allVisitors = Lists.newArrayList();

    for (Checks<DelphiChecks> checks : checksByRepository) {
      allVisitors.addAll(checks.all());
    }

    return allVisitors;
  }

  public List<SeCheck> seChecks() {
    List<SeCheck> checks = new ArrayList<>();
    for (DelphiChecks check : all()) {
      if (check instanceof SeCheck) {
        checks.add((SeCheck) check);
      }
    }

    return checks;
  }

  public List<TreeVisitor> visitorChecks() {
    List<TreeVisitor> checks = new ArrayList<>();
    for (DelphiChecks check : all()) {
      if (check instanceof TreeVisitor) {
        checks.add((TreeVisitor) check);
      }
    }

    return checks;
  }

  @Nullable
  public RuleKey ruleKeyFor(DelphiChecks check) {
    RuleKey ruleKey;

    for (Checks<DelphiChecks> checks : checksByRepository) {
      ruleKey = checks.ruleKey(check);

      if (ruleKey != null) {
        return ruleKey;
      }
    }
    return null;
  }

}