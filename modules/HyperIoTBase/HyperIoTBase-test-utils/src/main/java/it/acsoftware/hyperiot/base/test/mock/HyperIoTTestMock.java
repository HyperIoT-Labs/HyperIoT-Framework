/*
 * Copyright 2019-2023 ACSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.base.test.mock;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntityApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntitySystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;
import it.acsoftware.hyperiot.base.service.rest.HyperIoTBaseEntityRestApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HyperIoTTestMock {

    /**
     * This method create and returns a mocked instance of type T
     * @param entityApiClass    entityApi class
     * @param entityClass       entity class
     * @param <T>               HyperIoTBaseEntityApi concrete type
     * @param <S>               HyperIoTBaseEntity concrete type
     * @return                  a mocked instance of type T
     */
    public static <T extends HyperIoTBaseEntityApi<S>, S extends HyperIoTBaseEntity> HyperIoTBaseEntityApi<S>
    mockEntityApi(Class<T> entityApiClass, Class<S> entityClass) {

        HyperIoTBaseEntityApi<S> baseEntityApi = mock(entityApiClass);
        when(baseEntityApi.getEntityType()).thenReturn(entityClass);    // mock HyperIoTBaseEntity concrete type too
        return baseEntityApi;
    }
    /**
     * This class set entityRestApi property on entityRestApi instance
     * @param entityRestApi                 entityRestApi instance
     * @param entityRestApiClass            entityRestApi class
     * @param entityApi                     entityApi instance (often a mocked one)
     * @param entityApiClass                entityApi class
     * @param <T>                           HyperIoTBaseEntityApi concrete type
     * @param <U>                           HyperIoTBaseEntityApi concrete type
     * @param <S>                           HyperIoTBaseEntity concrete type
     * @throws NoSuchMethodException        ex
     * @throws InvocationTargetException    ex
     * @throws IllegalAccessException       ex
     */
    public static <T extends HyperIoTBaseEntityRestApi<S>, U extends HyperIoTBaseEntityApi<S>, S extends HyperIoTBaseEntity> void
    setEntityApi(T entityRestApi, Class<T> entityRestApiClass, U entityApi, Class<U> entityApiClass)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method setEntityServiceMethod =
                entityRestApiClass.getDeclaredMethod("setEntityService", entityApiClass);
        setEntityServiceMethod.setAccessible(true);
        setEntityServiceMethod.invoke(entityRestApi, entityApi);
    }

    /**
     * This class set an entityApi property on entityApi instance
     * @param entityApi                      entityApi instance to which inject entityApiInject
     * @param entityApiConcreteClass         HyperIoTBaseEntityApi concrete type
     * @param entityApiToInject              entityApi instance (often a mocked one) to inject on entityApi
     * @param <T>                            HyperIoTBaseEntityApi concrete type
     * @param <I>                            HyperIoTBaseEntityApi concrete type to Inject in T
     * @param <S>                            HyperIoTBaseEntity concrete type
     * @param <K>                            HyperIoTBaseEntity concrete type
     * @throws NoSuchMethodException         ex
     * @throws InvocationTargetException     ex
     * @throws IllegalAccessException        ex
     */
    public static <T extends HyperIoTBaseEntityApi<S>, I extends HyperIoTBaseEntityApi<K>, S extends HyperIoTBaseEntity, K extends HyperIoTBaseEntity> void
    setEntityApi(T entityApi, Class<T> entityApiConcreteClass, I entityApiToInject , Method injecterMethod)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method injectionMethod = entityApiConcreteClass.getDeclaredMethod(injecterMethod.getName(),injecterMethod.getParameterTypes());
        injectionMethod.setAccessible(true);
        injectionMethod.invoke(entityApi,entityApiToInject);

    }

    /**
     * This method create and returns a mocked instance of HyperIoTBaseEntitySystemApi of type T
     * @param entitySystemApiClass    entitySystemApiClass class
     * @param entityClass       entity class
     * @param <T>               HyperIoTBaseEntitySystemApi concrete type
     * @param <S>               HyperIoTBaseEntity concrete type
     * @return                  a mocked instance of type T
     */
    public static <T extends HyperIoTBaseEntitySystemApi<S>, S extends HyperIoTBaseEntity> HyperIoTBaseEntitySystemApi<S>
    mockEntitySystemApi(Class<T> entitySystemApiClass, Class<S> entityClass) {

        HyperIoTBaseEntitySystemApi<S> baseEntitySystemApi = mock(entitySystemApiClass);
        when(baseEntitySystemApi.getEntityType()).thenReturn(entityClass);    // mock HyperIoTBaseEntity concrete type too
        return baseEntitySystemApi;
    }

    /**
     * This class set an HyperIoTBaseEntitySystemApi on an HyperIoTBaseEntityApi
     * @param entityApi                      entityApi instance to which inject entitySystemApiToInject
     * @param entityApiConcreteClass         HyperIoTBaseEntityApi concrete type
     * @param entitySystemApiToInject              entitySystemApi instance (often a mocked one) to inject on entityApi
     * @param <T>                            HyperIoTBaseEntityApi concrete type
     * @param <I>                            HyperIoTBaseEntitySystemApi concrete type to Inject
     * @param <S>                            HyperIoTBaseEntity concrete type
     * @param <K>                            HyperIoTBaseEntity concrete type
     * @throws NoSuchMethodException         ex
     * @throws InvocationTargetException     ex
     * @throws IllegalAccessException        ex
     */
    public static <T extends HyperIoTBaseEntityApi<S>, I extends HyperIoTBaseEntitySystemApi<K>, S extends HyperIoTBaseEntity, K extends HyperIoTBaseEntity> void
    setEntitySystemApi(T entityApi, Class<T> entityApiConcreteClass, I entitySystemApiToInject, Method injecterMethod)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method injectionMethod = entityApiConcreteClass.getDeclaredMethod(injecterMethod.getName(),injecterMethod.getParameterTypes());
        injectionMethod.setAccessible(true);
        injectionMethod.invoke(entityApi,entitySystemApiToInject);

    }

    public static <T extends HyperIoTBaseEntitySystemApi<S>, I extends HyperIoTBaseEntitySystemApi<K>, S extends HyperIoTBaseEntity, K extends HyperIoTBaseEntity> void
    setEntitySystemApi(T entitySystemApi, Class<T> entitySystemApiConcreteClass, I entitySystemApiToInject, Method injecterMethod)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method injectionMethod = entitySystemApiConcreteClass.getDeclaredMethod(injecterMethod.getName(),injecterMethod.getParameterTypes());
        injectionMethod.setAccessible(true);
        injectionMethod.invoke(entitySystemApi,entitySystemApiToInject);
    }

    public static <T extends HyperIoTBaseEntitySystemApi<S>, I extends HyperIoTBaseRepository<K>, S extends HyperIoTBaseEntity, K extends HyperIoTBaseEntity> void
    setRepository(T entitySystemApi, Class<T> entitySystemApiConcreteClass, I repositoryToInject, Method injecterMethod)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method injectionMethod = entitySystemApiConcreteClass.getDeclaredMethod(injecterMethod.getName(),injecterMethod.getParameterTypes());
        injectionMethod.setAccessible(true);
        injectionMethod.invoke(entitySystemApi,repositoryToInject);

    }

    public static <T extends HyperIoTBaseRepository<S>, S extends HyperIoTBaseEntity> HyperIoTBaseRepository<S>
    mockRepository(Class<T> repositoryClass, Class<S> entityClass) {
        return  mock(repositoryClass);
    }



}
