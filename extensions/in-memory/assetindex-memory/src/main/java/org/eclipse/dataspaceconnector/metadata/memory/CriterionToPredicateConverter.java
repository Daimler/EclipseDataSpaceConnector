package org.eclipse.dataspaceconnector.metadata.memory;

import org.eclipse.dataspaceconnector.spi.asset.Criterion;
import org.eclipse.dataspaceconnector.spi.asset.CriterionConverter;
import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Predicate;

import static java.lang.String.format;

/**
 * Converts a {@link Criterion}, which is essentially a select statement, into a {@code Predicate<Asset>}.
 * <p>
 * This is useful when dealing with in-memory collections of objects, here: {@link Asset} where Predicates can be applied
 * efficiently.
 * <p>
 * _Note: other {@link org.eclipse.dataspaceconnector.spi.asset.AssetIndex} implementations might have different converters!
 */
class CriterionToPredicateConverter implements CriterionConverter<Predicate<Asset>> {
    @Override
    public Predicate<Asset> convert(Criterion criterion) {
        var isEqualsOperator = "=".equals(criterion.getOperator());
        if (!isEqualsOperator) {
            throw new IllegalArgumentException(format("Operator [%s] is not supported by this converter!", criterion.getOperator()));
        }

        var isSelectAllCriterion = criterion.getOperandLeft().equals("*") && criterion.getOperandRight().equals("*");
        if (isSelectAllCriterion) {
            return asset -> true;
        }

        return asset -> Objects.equals(field(criterion.getOperandLeft(), asset), criterion.getOperandRight()) ||
                Objects.equals(label(criterion.getOperandLeft(), asset), criterion.getOperandRight());
    }

    private Object field(String fieldName, Asset asset) {
        try {
            Field declaredField = asset.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(asset);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    private String label(String key, Asset asset) {
        if (asset.getProperties() == null || !asset.getProperties().isEmpty()) {
            return null;
        }
        return asset.getProperties().get(key);
    }
}
