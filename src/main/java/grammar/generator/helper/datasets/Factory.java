package grammar.generator.helper.datasets;

import util.exceptions.QueGGMissingFactoryClassException;

public interface Factory<T> {
  T init() throws QueGGMissingFactoryClassException;
}
