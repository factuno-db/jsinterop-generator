/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package jsinterop.generator.visitor;

import static jsinterop.generator.model.PredefinedTypeReference.DOUBLE;
import static jsinterop.generator.model.PredefinedTypeReference.INT;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import jsinterop.generator.helper.Problems;
import jsinterop.generator.model.Field;
import jsinterop.generator.model.Method;
import jsinterop.generator.model.Program;
import jsinterop.generator.model.TypeReference;

/** Handles number javascript entities that need to be converted to int instead of double. */
public class IntegerEntitiesConverter extends AbstractModelVisitor {
  private final Set<String> integerEntities;
  private final Set<String> unusedIntegerEntities;
  private final Problems problems;

  private String currentFqn;

  public IntegerEntitiesConverter(List<String> integerEntities, Problems problems) {
    this.integerEntities = ImmutableSet.copyOf(integerEntities);
    // use of LinkedHashSet for always reporting unused entities in the same order
    this.unusedIntegerEntities = new LinkedHashSet<>(integerEntities);
    this.problems = problems;
  }

  @Override
  public boolean visit(Program program) {
    return !integerEntities.isEmpty();
  }

  @Override
  public void endVisit(Program program) {
    if (!unusedIntegerEntities.isEmpty()) {
      problems.reportWarning("Unused integer entities: %s", unusedIntegerEntities);
    }
  }

  @Override
  public boolean visit(Field field) {
    currentFqn = field.getJavaFqn();
    return true;
  }

  @Override
  public boolean visit(Method.Parameter parameter) {
    currentFqn = parameter.getJavaFqn();
    return true;
  }

  @Override
  public boolean visit(Method method) {
    currentFqn = method.getJavaFqn();
    return true;
  }

  private boolean mustBeConvertedToInt(TypeReference originalTypeReference) {
    return integerEntities.contains(currentFqn) && DOUBLE.equals(originalTypeReference);
  }

  @Override
  public TypeReference endVisit(TypeReference typeReference) {
    if (mustBeConvertedToInt(typeReference)) {
      unusedIntegerEntities.remove(currentFqn);
      return INT;
    }

    return typeReference;
  }
}
