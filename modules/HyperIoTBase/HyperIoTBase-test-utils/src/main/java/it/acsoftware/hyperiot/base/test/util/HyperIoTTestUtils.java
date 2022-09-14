package it.acsoftware.hyperiot.base.test.util;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;

import java.util.Collection;
import java.util.function.Function;

public class HyperIoTTestUtils {

    public static <T extends HyperIoTBaseEntity> void truncateTables(HyperIoTBaseRepository<T> baseRepository, Function<T, Boolean> filter) {
        Collection<T> allEntities = baseRepository.findAll(null);
        if (filter == null) {
            filter = (entity) -> {
                return true;
            };
        }
        final Function<T, Boolean> filterFunction = filter;
        allEntities.stream().filter(entity -> filterFunction.apply(entity)).forEach(toDelete ->
                baseRepository.remove(toDelete.getId())
        );
    }

    public static <T extends HyperIoTBaseEntity> void truncateTables(HyperIoTBaseEntitySystemApi<T> systemApi, Function<T, Boolean> filter) {
        Collection<T> allEntities = systemApi.findAll(null, null);
        if (filter == null) {
            filter = (entity) -> {
                return true;
            };
        }
        final Function<T, Boolean> filterFunction = filter;
        allEntities.stream().filter(entity -> filterFunction.apply(entity)).forEach(toDelete ->
                systemApi.remove(toDelete.getId(), null)
        );
    }
}
